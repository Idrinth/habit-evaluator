package de.idrinth.habitevaluator.android.coverage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Debug-only broadcast receiver that dumps JaCoCo execution data to the app's
 * internal files directory, accessible via {@code adb exec-out run-as <package>}
 * on debuggable builds across all API levels (including API 30+ where scoped
 * storage restricts adb pull from external storage).
 * Triggered via: adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE
 *                --es coverageFile coverage.ec
 */
public class CoverageBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CoverageBroadcastReceiver";
    private static final String DEFAULT_COVERAGE_FILENAME = "coverage.ec";

    @Override
    public void onReceive(Context context, Intent intent) {
        String coverageFileName = intent.getStringExtra("coverageFile");
        if (coverageFileName == null || coverageFileName.isEmpty()) {
            coverageFileName = DEFAULT_COVERAGE_FILENAME;
        }
        File internalDir = context.getFilesDir();
        if (internalDir == null) {
            Log.e(TAG, "Internal files directory not available");
            return;
        }
        File coverageFile = new File(internalDir, coverageFileName);
        dumpCoverage(coverageFile);
    }

    private void dumpCoverage(File coverageFile) {
        try {
            // AGP uses offline instrumentation, so coverage data is stored in
            // org.jacoco.agent.rt.internal.Offline, not accessible via RT.getAgent().
            // Offline.getExecutionData(boolean) is a static method available since
            // JaCoCo 0.8.8 that returns serialized execution data directly.
            Class<?> offlineClass = Class.forName("org.jacoco.agent.rt.internal.Offline");
            Method getExecutionDataMethod = offlineClass.getMethod("getExecutionData", boolean.class);
            byte[] data = (byte[]) getExecutionDataMethod.invoke(null, false);

            try (OutputStream out = new FileOutputStream(coverageFile)) {
                out.write(data);
            }
            Log.d(TAG, "Coverage data written to " + coverageFile.getAbsolutePath()
                    + " (" + data.length + " bytes)");
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "JaCoCo Offline class not available — app may not be instrumented");
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, "Failed to invoke JaCoCo Offline.getExecutionData()", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write coverage data to " + coverageFile.getAbsolutePath(), e);
        }
    }
}
