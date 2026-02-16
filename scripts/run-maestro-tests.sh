#!/bin/bash

PACKAGE="de.idrinth.habitevaluator.android"

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?
adb shell am broadcast -a "$PACKAGE.DUMP_COVERAGE" --es coverageFile "coverage_$1.ec" || true
sleep 2
adb exec-out run-as "$PACKAGE" cat "files/coverage_$1.ec" > "android/build/maestro-coverage/coverage_$1.ec" 2>/dev/null || true
# Strip any adb/run-as output prefixed before the actual JaCoCo binary data
EC_FILE="android/build/maestro-coverage/coverage_$1.ec"
if [ -s "$EC_FILE" ]; then
    python3 -c "
import sys
data = open(sys.argv[1], 'rb').read()
idx = data.find(b'\x01\xc0\xc0')
if idx < 0:
    sys.exit(1)
if idx > 0:
    open(sys.argv[1], 'wb').write(data[idx:])
" "$EC_FILE" || {
        rm -f "$EC_FILE"
        echo "::warning::Coverage file for $1 is not valid JaCoCo data"
    }
elif [ -f "$EC_FILE" ]; then
    rm -f "$EC_FILE"
    echo "::warning::Coverage file for $1 was not produced"
fi
if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
