#!/bin/bash

PACKAGE="de.idrinth.habitevaluator.android"
COVERAGE_DIR="/sdcard/Android/data/$PACKAGE/files"

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_EXIT=0
maestro test "android/.maestro/$1.yaml" || MAESTRO_EXIT=$?
adb shell am broadcast -a "$PACKAGE.DUMP_COVERAGE" --es coverageFile "coverage_$1.ec" || true
sleep 2
adb pull "$COVERAGE_DIR/coverage_$1.ec" "android/build/maestro-coverage/" 2>/dev/null || true
if [ "$MAESTRO_EXIT" -ne 0 ]; then
    echo "::error::Maestro test $1 failed with exit code $MAESTRO_EXIT"
fi
exit $MAESTRO_EXIT
