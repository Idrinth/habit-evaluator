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
EXTERNAL_DIR="/sdcard/Android/data/$PACKAGE/files"

# Retry coverage collection up to 5 times. The broadcast receiver uses
# goAsync() and a background thread, but the broadcast itself is still
# enqueued on the main thread's Looper. Activities with heavy custom view
# rendering (e.g. SleepAnalysisActivity with graph views) can delay delivery
# on slow emulators using software rendering (swiftshader_indirect).
MAX_ATTEMPTS=5
COVERAGE_COLLECTED=""

for ATTEMPT in $(seq 1 "$MAX_ATTEMPTS"); do
    # Verify the app process is still alive — coverage data lives in the
    # JVM's static fields and is lost if the process dies.
    APP_PID=$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r')
    if [ -z "$APP_PID" ]; then
        echo "::warning::App process not running before coverage dump (attempt $ATTEMPT/$MAX_ATTEMPTS)"
        # Try to relaunch the app so coverage classes are loaded for the broadcast
        adb shell am start -n "$PACKAGE/.MainActivity" 2>/dev/null || true
        sleep 5
        APP_PID=$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r')
        if [ -z "$APP_PID" ]; then
            echo "::warning::Could not restart app for coverage dump"
            break
        fi
        # Verify the process is stable (hasn't crashed immediately)
        sleep 3
        APP_PID2=$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r')
        if [ -z "$APP_PID2" ]; then
            echo "::warning::App process died shortly after restart (attempt $ATTEMPT/$MAX_ATTEMPTS)"
            continue
        fi
    fi

    # Remove any previous status files so we can tell whether the receiver
    # ran this attempt.
    adb shell run-as "$PACKAGE" rm -f "files/$STATUS_FILENAME" 2>/dev/null || true
    adb shell rm -f "$EXTERNAL_DIR/$STATUS_FILENAME" 2>/dev/null || true

    # Bring the app to the foreground before each attempt so the main-thread
    # Looper is actively dispatching messages.  On slow emulators using
    # swiftshader_indirect the Looper can stall when the app is backgrounded,
    # causing broadcast delivery to time out silently.
    adb shell am start -W -n "$PACKAGE/.MainActivity" >/dev/null 2>&1 || true
    sleep 1

    # Clear logcat before each attempt so status detection only sees messages
    # from this attempt, not stale ones from a prior retry.
    adb logcat -c 2>/dev/null || true

    # Send broadcast with both component target (-n) and explicit action (-a)
    # to ensure reliable delivery across Android versions. The action matches
    # the intent-filter in the debug AndroidManifest.
    # Flags:
    #   0x00000020  FLAG_INCLUDE_STOPPED_PACKAGES — deliver even if the system
    #               considers the app to be in a stopped state (which can
    #               happen after a crash + restart on some API levels).
    # NOTE: FLAG_RECEIVER_FOREGROUND (0x10000000) is intentionally NOT used.
    # The foreground broadcast queue enforces a 10-second hard timeout, which
    # is too short for JaCoCo coverage dumps on slow emulators using software
    # rendering (swiftshader_indirect). The background queue allows 60 seconds.
    # Broadcast deferral is not a concern because the script brings the app to
    # the foreground before sending the broadcast.
    BROADCAST_OUTPUT=$(adb shell am broadcast \
      -a "$BROADCAST_ACTION" \
      -n "$PACKAGE/.coverage.CoverageBroadcastReceiver" \
      -f 0x00000020 \
      --es coverageFile "$COVERAGE_FILENAME" 2>&1) || true

    # Log broadcast result for diagnostics
    if echo "$BROADCAST_OUTPUT" | grep -qi "error\|exception\|not found"; then
        echo "::warning::Broadcast delivery issue on attempt $ATTEMPT: $BROADCAST_OUTPUT"
    fi

    # Wait for the receiver to process the broadcast and write the file.
    # The receiver now uses goAsync() + background thread so the main thread
    # is freed quickly, but we still need to wait for the thread to complete.
    SLEEP_DURATION=$((4 + ATTEMPT * 2))
    sleep "$SLEEP_DURATION"

    # PRIMARY STATUS CHECK: logcat (does not depend on run-as working)
    # The receiver logs structured "COVERAGE_RESULT:<status>" messages.
    LOGCAT_STATUS=$(adb logcat -d -s CoverageBroadcastReceiver:* 2>/dev/null \
      | grep "COVERAGE_RESULT" | tail -1 | tr -d '\r')

    if echo "$LOGCAT_STATUS" | grep -q "COVERAGE_RESULT:OK:"; then
        : # Receiver ran successfully, coverage file should exist
    elif echo "$LOGCAT_STATUS" | grep -q "COVERAGE_RESULT:EMPTY"; then
        echo "::warning::Coverage receiver reported EMPTY data (attempt $ATTEMPT/$MAX_ATTEMPTS for $1)"
        echo "::warning::Bytecode may not be instrumented or process was restarted — skipping remaining retries"
        break
    elif echo "$LOGCAT_STATUS" | grep -q "COVERAGE_RESULT:NO_CLASS"; then
        echo "::warning::Coverage receiver reported NO_CLASS (attempt $ATTEMPT/$MAX_ATTEMPTS for $1)"
        echo "::warning::Instrumentation issue detected — skipping remaining retries"
        break
    elif echo "$LOGCAT_STATUS" | grep -qE "COVERAGE_RESULT:(IO_ERROR|UNEXPECTED|ERROR)"; then
        echo "::warning::Coverage receiver reported error: $LOGCAT_STATUS (attempt $ATTEMPT/$MAX_ATTEMPTS for $1)"
        if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
            sleep 2
            continue
        fi
        break
    else
        # FALLBACK STATUS CHECK: status file via run-as (original method)
        STATUS_CHECK=$(adb shell run-as "$PACKAGE" cat "files/$STATUS_FILENAME" 2>&1 | tr -d '\r')
        if echo "$STATUS_CHECK" | grep -q "^OK:"; then
            : # Receiver ran successfully, coverage file should exist
        elif echo "$STATUS_CHECK" | grep -qE "^(EMPTY|NO_CLASS|REFLECT_ERROR|IO_ERROR|UNEXPECTED):"; then
            echo "::warning::Coverage receiver reported: $STATUS_CHECK (attempt $ATTEMPT/$MAX_ATTEMPTS for $1)"
            if echo "$STATUS_CHECK" | grep -qE "^(NO_CLASS|EMPTY):"; then
                echo "::warning::Instrumentation issue detected — skipping remaining retries for $1"
                break
            fi
            if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
                sleep 2
                continue
            fi
            break
        elif echo "$STATUS_CHECK" | grep -q "^STARTED"; then
            # Receiver ran (heartbeat written) but the JaCoCo dump is still
            # in progress on the background thread.  Give it more time.
            if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
                echo "::warning::Coverage receiver started but dump not finished yet (attempt $ATTEMPT/$MAX_ATTEMPTS for $1), retrying..."
                sleep 4
                continue
            fi
        else
            # Neither logcat nor status file showed a result — receiver may
            # not have run yet.
            if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
                echo "::warning::Coverage receiver has not responded yet (attempt $ATTEMPT/$MAX_ATTEMPTS for $1), retrying..."
                sleep 2
                continue
            fi
            # Final attempt: pull logcat and package info to diagnose why
            echo "::warning::Coverage receiver never responded for $1 after $MAX_ATTEMPTS attempts"
            echo "::group::Logcat (CoverageBroadcastReceiver)"
            adb logcat -d -s CoverageBroadcastReceiver:* 2>/dev/null | tail -30
            echo "::endgroup::"
            echo "::group::System logcat (broadcast delivery)"
            adb logcat -d -s ActivityManager:* BroadcastQueue:* AndroidRuntime:* System.err:* 2>/dev/null | tail -50
            echo "::endgroup::"
            echo "::group::Broadcast diagnostic"
            echo "Last broadcast output: $BROADCAST_OUTPUT"
            echo "Receiver registration:"
            adb shell dumpsys package "$PACKAGE" 2>/dev/null | grep -A3 "CoverageBroadcastReceiver" || echo "Receiver not found in package dump"
            echo "App process:"
            adb shell pidof "$PACKAGE" 2>/dev/null || echo "App process not running"
            echo "run-as test:"
            adb shell run-as "$PACKAGE" ls files/ 2>&1 | head -5
            echo "::endgroup::"
            break
        fi
    fi

    # Attempt to extract the coverage file.
    # PRIMARY: adb pull from external storage (does not depend on run-as)
    adb pull "$EXTERNAL_DIR/$COVERAGE_FILENAME" "$EC_FILE" 2>/dev/null

    if [ -s "$EC_FILE" ]; then
        COVERAGE_COLLECTED=1
        break
    fi

    # FALLBACK: Use base64 encoding via run-as for extraction from internal
    # storage. Filter output: strip \r (injected by some adb versions) and
    # drop any non-base64 lines (e.g. linker warnings, run-as messages).
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

    ENCODED=$(adb exec-out run-as "$PACKAGE" base64 "files/$COVERAGE_FILENAME" 2>/dev/null \
      | tr -d '\r' \
      | grep -E '^[A-Za-z0-9+/=]+$')

    if [ -n "$ENCODED" ]; then
        echo "$ENCODED" | base64 -d > "$EC_FILE" 2>/dev/null
        COVERAGE_COLLECTED=1
        break
    fi

    if [ "$ATTEMPT" -lt "$MAX_ATTEMPTS" ]; then
        echo "::warning::Coverage extraction attempt $ATTEMPT failed for $1, retrying..."
        sleep 2
    fi
done

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
    rm -f "$EC_FILE" 2>/dev/null
    echo "::warning::Coverage file for $1 was not produced"
fi

if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
