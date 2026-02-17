#!/bin/bash

PACKAGE="de.idrinth.habitevaluator.android"

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?

EC_FILE="android/build/maestro-coverage/coverage_$1.ec"

# Retry coverage collection up to 3 times. Some tests (e.g. those with
# heavy scrolling) leave the main thread busy; the broadcast may need an
# extra attempt before the file appears on disk.
MAX_ATTEMPTS=3
ENCODED=""

for ATTEMPT in $(seq 1 "$MAX_ATTEMPTS"); do
    # Verify the app process is still alive — coverage data lives in the
    # JVM's static fields and is lost if the process dies.
    APP_PID=$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r')
    if [ -z "$APP_PID" ]; then
        echo "::warning::App process not running before coverage dump (attempt $ATTEMPT/$MAX_ATTEMPTS)"
        break
    fi

    # Send explicit broadcast (component-targeted) to dump coverage data
    adb shell am broadcast \
      -n "$PACKAGE/.coverage.CoverageBroadcastReceiver" \
      --es coverageFile "coverage_$1.ec" || true
    sleep 3

    # Use base64 encoding for extraction to avoid binary data corruption
    # through the adb exec-out pipe (raw binary piping can lose or mangle
    # bytes containing null or control characters on some adb versions).
    # Filter output: strip \r (injected by some adb versions) and drop any
    # non-base64 lines (e.g. linker warnings, run-as messages) that the
    # device may emit to stdout before the actual data.
    ENCODED=$(adb exec-out run-as "$PACKAGE" base64 "files/coverage_$1.ec" 2>/dev/null \
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
