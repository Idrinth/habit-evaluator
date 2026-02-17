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
 * <p>
 * Uses {@link #goAsync()} so that the dump runs on a background thread,
 * preventing the main-thread from blocking delivery when the UI is busy with
 * heavy view rendering (e.g. custom chart views on a software-rendered
 * emulator).
 * <p>
 * In addition to the {@code .ec} coverage file, the receiver always writes a
 * small {@code .status} file (same base name) that the collection script can
 * check to distinguish "receiver never ran" from "receiver ran but had no
 * data."
 * <p>
 * Triggered via: adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE
 *                --es coverageFile coverage.ec
 */
public class CoverageBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CoverageBroadcastReceiver";
    private static final String DEFAULT_COVERAGE_FILENAME = "coverage.ec";

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();

        String coverageFileName = intent.getStringExtra("coverageFile");
        if (coverageFileName == null || coverageFileName.isEmpty()) {
            coverageFileName = DEFAULT_COVERAGE_FILENAME;
        }
        File internalDir = context.getFilesDir();
        if (internalDir == null) {
            Log.e(TAG, "Internal files directory not available");
            writeStatus(null, "ERROR: internal files directory not available");
            pendingResult.finish();
            return;
        }
        File coverageFile = new File(internalDir, coverageFileName);
        File statusFile = statusFileFor(internalDir, coverageFileName);

        final File fCoverageFile = coverageFile;
        final File fStatusFile = statusFile;
        new Thread(() -> {
            try {
                dumpCoverage(fCoverageFile, fStatusFile);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private void dumpCoverage(File coverageFile, File statusFile) {
        try {
            // AGP uses offline instrumentation, so coverage data is stored in
            // org.jacoco.agent.rt.internal.Offline, not accessible via RT.getAgent().
            // Offline.getExecutionData(boolean) is a static method available since
            // JaCoCo 0.8.8 that returns serialized execution data directly.
            Class<?> offlineClass = Class.forName("org.jacoco.agent.rt.internal.Offline");
            Method getExecutionDataMethod = offlineClass.getMethod("getExecutionData", boolean.class);
            byte[] data = (byte[]) getExecutionDataMethod.invoke(null, false);

            if (data == null || data.length == 0) {
                String msg = "JaCoCo returned empty execution data — bytecode may not be instrumented";
                Log.w(TAG, msg);
                writeStatus(statusFile, "EMPTY: " + msg);
                return;
            }

            try (OutputStream out = new FileOutputStream(coverageFile)) {
                out.write(data);
                out.flush();
            }
            Log.d(TAG, "Coverage data written to " + coverageFile.getAbsolutePath()
                    + " (" + data.length + " bytes)");
            writeStatus(statusFile, "OK: " + data.length + " bytes");
        } catch (ClassNotFoundException e) {
            String msg = "JaCoCo Offline class not available — app may not be instrumented";
            Log.w(TAG, msg);
            writeStatus(statusFile, "NO_CLASS: " + msg);
        } catch (ReflectiveOperationException e) {
            String msg = "Failed to invoke JaCoCo Offline.getExecutionData(): " + e.getMessage();
            Log.e(TAG, msg, e);
            writeStatus(statusFile, "REFLECT_ERROR: " + msg);
        } catch (IOException e) {
            String msg = "Failed to write coverage data to " + coverageFile.getAbsolutePath()
                    + ": " + e.getMessage();
            Log.e(TAG, msg, e);
            writeStatus(statusFile, "IO_ERROR: " + msg);
        } catch (Exception e) {
            String msg = "Unexpected error during coverage dump: " + e.getMessage();
            Log.e(TAG, msg, e);
            writeStatus(statusFile, "UNEXPECTED: " + msg);
        }
    }

    private static File statusFileFor(File dir, String coverageFileName) {
        String base = coverageFileName.endsWith(".ec")
                ? coverageFileName.substring(0, coverageFileName.length() - 3)
                : coverageFileName;
        return new File(dir, base + ".status");
    }

    private static void writeStatus(File statusFile, String message) {
        if (statusFile == null) {
            return;
        }
        try (OutputStream out = new FileOutputStream(statusFile)) {
            out.write(message.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write status file", e);
        }
    }
}
