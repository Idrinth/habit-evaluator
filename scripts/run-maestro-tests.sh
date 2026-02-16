#!/bin/bash

PACKAGE="de.idrinth.habitevaluator.android"

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?
adb shell am broadcast -a "$PACKAGE.DUMP_COVERAGE" --es coverageFile "coverage_$1.ec" || true
sleep 2
adb exec-out run-as "$PACKAGE" cat "files/coverage_$1.ec" > "android/build/maestro-coverage/coverage_$1.ec" 2>/dev/null || true
# Remove empty or invalid coverage files
if [ -f "android/build/maestro-coverage/coverage_$1.ec" ]; then
    if [ ! -s "android/build/maestro-coverage/coverage_$1.ec" ]; then
        rm -f "android/build/maestro-coverage/coverage_$1.ec"
        echo "::warning::Coverage file for $1 was not produced"
    else
        # Validate JaCoCo execution data magic bytes (0x01 0xC0 0xC0)
        MAGIC=$(od -A n -t x1 -N 3 "android/build/maestro-coverage/coverage_$1.ec" 2>/dev/null | tr -d ' \n')
        if [ "$MAGIC" != "01c0c0" ]; then
            rm -f "android/build/maestro-coverage/coverage_$1.ec"
            echo "::warning::Coverage file for $1 is not valid JaCoCo data"
        fi
    fi
fi
if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
