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
            Class<?> rtClass = Class.forName("org.jacoco.agent.rt.RT");
            Method getAgentMethod = rtClass.getMethod("getAgent");
            Object agent = getAgentMethod.invoke(null);

            Method getExecutionDataMethod = agent.getClass()
                    .getMethod("getExecutionData", boolean.class);
            byte[] data = (byte[]) getExecutionDataMethod.invoke(agent, false);

            try (OutputStream out = new FileOutputStream(coverageFile)) {
                out.write(data);
            }
            Log.d(TAG, "Coverage data written to " + coverageFile.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "JaCoCo agent not available — app may not be instrumented");
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, "Failed to invoke JaCoCo agent", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write coverage data to " + coverageFile.getAbsolutePath(), e);
        }
    }
}
