#!/bin/bash

adb install android/build/outputs/apk/oreo/debug/*.apk
mkdir -p android/build/maestro-coverage
MAESTRO_FAILED=0
for test_file in android/.maestro/*.yaml; do
  maestro test "$test_file" || MAESTRO_FAILED=1
  adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE --es coverageFile "/sdcard/coverage_$(basename "$test_file" .yaml).ec"
  sleep 1
  adb pull "/sdcard/coverage_$(basename "$test_file" .yaml).ec" "android/build/maestro-coverage/" 2>/dev/null || true
done
exit $MAESTRO_FAILED
