#!/bin/bash

PACKAGE="de.idrinth.habitevaluator.android"

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?

EC_FILE="android/build/maestro-coverage/coverage_$1.ec"
COVERAGE_FILENAME="coverage_$1.ec"
STATUS_FILENAME="coverage_$1.status"
BROADCAST_ACTION="de.idrinth.habitevaluator.android.DUMP_COVERAGE"

# Retry coverage collection up to 5 times. The broadcast receiver uses
# goAsync() and a background thread, but the broadcast itself is still
# enqueued on the main thread's Looper. Activities with heavy custom view
# rendering (e.g. SleepAnalysisActivity with graph views) can delay delivery
# on slow emulators using software rendering (swiftshader_indirect).
MAX_ATTEMPTS=5
ENCODED=""

for ATTEMPT in $(seq 1 "$MAX_ATTEMPTS"); do
    # Verify the app process is still alive — coverage data lives in the
    # JVM's static fields and is lost if the process dies.
    APP_PID=$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r')
    if [ -z "$APP_PID" ]; then
        echo "::warning::App process not running before coverage dump (attempt $ATTEMPT/$MAX_ATTEMPTS)"
        # Try to relaunch the app so coverage classes are loaded for the broadcast
        adb shell am start -n "$PACKAGE/.MainActivity" 2>/dev/null || true
        sleep 3
        APP_PID=$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r')
        if [ -z "$APP_PID" ]; then
            echo "::warning::Could not restart app for coverage dump"
            break
        fi
    fi

    # Remove any previous status file so we can tell whether the receiver ran
    # this attempt.
    adb shell run-as "$PACKAGE" rm -f "files/$STATUS_FILENAME" 2>/dev/null || true

    # Send broadcast with both component target (-n) and explicit action (-a)
    # to ensure reliable delivery across Android versions. The action matches
    # the intent-filter in the debug AndroidManifest.
    BROADCAST_OUTPUT=$(adb shell am broadcast \
      -a "$BROADCAST_ACTION" \
      -n "$PACKAGE/.coverage.CoverageBroadcastReceiver" \
      --es coverageFile "$COVERAGE_FILENAME" 2>&1) || true

    # Log broadcast result for diagnostics
    if echo "$BROADCAST_OUTPUT" | grep -qi "error\|exception"; then
        echo "::warning::Broadcast delivery issue on attempt $ATTEMPT: $BROADCAST_OUTPUT"
    fi

    # Wait for the receiver to process the broadcast and write the file.
    # The receiver now uses goAsync() + background thread so the main thread
    # is freed quickly, but we still need to wait for the thread to complete.
    SLEEP_DURATION=$((3 + ATTEMPT))
    sleep "$SLEEP_DURATION"

    # Check the .status file first — the receiver writes this regardless of
    # whether coverage data was produced. If the status file exists, we know
    # the receiver ran; its content tells us why coverage may be missing.
    STATUS_CHECK=$(adb shell run-as "$PACKAGE" cat "files/$STATUS_FILENAME" 2>&1 | tr -d '\r')
    if echo "$STATUS_CHECK" | grep -q "^OK:"; then
        : # Receiver ran successfully, coverage file should exist
    elif echo "$STATUS_CHECK" | grep -qE "^(EMPTY|NO_CLASS|REFLECT_ERROR|IO_ERROR|UNEXPECTED):"; then
        echo "::warning::Coverage receiver reported: $STATUS_CHECK (attempt $ATTEMPT/$MAX_ATTEMPTS for $1)"
        # No point retrying if the receiver itself says instrumentation is
        # missing — the data will not appear on subsequent attempts.
        if echo "$STATUS_CHECK" | grep -qE "^(NO_CLASS|EMPTY):"; then
            echo "::warning::Instrumentation issue detected — skipping remaining retries for $1"
            break
        fi
        if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
            sleep 2
            continue
        fi
        break
    else
        # Status file not found or unreadable — receiver may not have run yet
        if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
            echo "::warning::Coverage receiver has not responded yet (attempt $ATTEMPT/$MAX_ATTEMPTS for $1), retrying..."
            sleep 2
            continue
        fi
        # Final attempt: pull logcat to help diagnose why the receiver never ran
        echo "::warning::Coverage receiver never responded for $1 after $MAX_ATTEMPTS attempts"
        echo "::group::Logcat (CoverageBroadcastReceiver)"
        adb logcat -d -s CoverageBroadcastReceiver:* 2>/dev/null | tail -30
        echo "::endgroup::"
        break
    fi

    # Verify the coverage file exists on the device before extraction
    FILE_CHECK=$(adb shell run-as "$PACKAGE" ls "files/$COVERAGE_FILENAME" 2>&1 | tr -d '\r')
    if echo "$FILE_CHECK" | grep -q "No such file"; then
        if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
            echo "::warning::Coverage file not yet created on device (attempt $ATTEMPT/$MAX_ATTEMPTS for $1), retrying..."
            sleep 2
            continue
        fi
        echo "::warning::Coverage file never created on device for $1 after $MAX_ATTEMPTS attempts"
        break
    fi

    # Use base64 encoding for extraction to avoid binary data corruption
    # through the adb exec-out pipe (raw binary piping can lose or mangle
    # bytes containing null or control characters on some adb versions).
    # Filter output: strip \r (injected by some adb versions) and drop any
    # non-base64 lines (e.g. linker warnings, run-as messages) that the
    # device may emit to stdout before the actual data.
    ENCODED=$(adb exec-out run-as "$PACKAGE" base64 "files/$COVERAGE_FILENAME" 2>/dev/null \
      | tr -d '\r' \
      | grep -E '^[A-Za-z0-9+/=]+$')

    if [ -n "$ENCODED" ]; then
        break
    fi

    if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
        echo "::warning::Coverage extraction attempt $ATTEMPT failed for $1, retrying..."
        sleep 2
    fi
done

if [ -n "$ENCODED" ]; then
    echo "$ENCODED" | base64 -d > "$EC_FILE" 2>/dev/null
    if [ -s "$EC_FILE" ]; then
        python3 -c "
import sys
data = open(sys.argv[1], 'rb').read()
if len(data) < 5 or data[0:3] != b'\x01\xc0\xc0':
    sys.exit(1)
" "$EC_FILE" || {
            rm -f "$EC_FILE"
            echo "::warning::Coverage file for $1 is not valid JaCoCo data"
        }
    else
        rm -f "$EC_FILE"
        echo "::warning::Coverage file for $1 could not be decoded"
    fi
else
    echo "::warning::Coverage file for $1 was not produced"
fi

if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
