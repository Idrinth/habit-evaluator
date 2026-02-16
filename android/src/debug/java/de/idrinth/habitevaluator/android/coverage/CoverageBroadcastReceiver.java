package de.idrinth.habitevaluator.android.coverage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Debug-only broadcast receiver that dumps JaCoCo execution data to external storage.
 * Triggered via: adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE
 *                --es coverageFile /sdcard/coverage.ec
 */
public class CoverageBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CoverageBroadcastReceiver";
    private static final String DEFAULT_COVERAGE_FILE =
            Environment.getExternalStorageDirectory().getPath() + "/coverage.ec";

    @Override
    public void onReceive(Context context, Intent intent) {
        String coverageFile = intent.getStringExtra("coverageFile");
        if (coverageFile == null || coverageFile.isEmpty()) {
            coverageFile = DEFAULT_COVERAGE_FILE;
        }
        dumpCoverage(coverageFile);
    }

    private void dumpCoverage(String coverageFilePath) {
        try {
            Class<?> rtClass = Class.forName("org.jacoco.agent.rt.RT");
            Method getAgentMethod = rtClass.getMethod("getAgent");
            Object agent = getAgentMethod.invoke(null);

            Method getExecutionDataMethod = agent.getClass()
                    .getMethod("getExecutionData", boolean.class);
            byte[] data = (byte[]) getExecutionDataMethod.invoke(agent, false);

            try (OutputStream out = new FileOutputStream(coverageFilePath)) {
                out.write(data);
            }
            Log.d(TAG, "Coverage data written to " + coverageFilePath);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "JaCoCo agent not available — app may not be instrumented");
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, "Failed to invoke JaCoCo agent", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write coverage data to " + coverageFilePath, e);
        }
    }
}
