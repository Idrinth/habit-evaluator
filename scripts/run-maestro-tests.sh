#!/bin/bash

PACKAGE="de.idrinth.habitevaluator.android"

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?
adb shell am broadcast -a "$PACKAGE.DUMP_COVERAGE" --es coverageFile "coverage_$1.ec" || true
sleep 2
adb exec-out run-as "$PACKAGE" cat "files/coverage_$1.ec" > "android/build/maestro-coverage/coverage_$1.ec" 2>/dev/null || true
# Remove empty file if pull failed
if [ ! -s "android/build/maestro-coverage/coverage_$1.ec" ]; then
    rm -f "android/build/maestro-coverage/coverage_$1.ec"
    echo "::warning::Coverage file for $1 was not produced"
fi
if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
