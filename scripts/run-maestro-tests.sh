#!/bin/bash

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?
adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE --es coverageFile "/sdcard/coverage_${{ matrix.test-file }}.ec" || true
sleep 1
adb pull "/sdcard/coverage_$1.ec" "android/build/maestro-coverage/" 2>/dev/null || true
if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
