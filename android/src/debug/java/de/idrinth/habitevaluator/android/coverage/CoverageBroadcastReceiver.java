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
 * files directory, accessible via {@code adb pull} (external) or
 * {@code adb exec-out run-as <package>} (internal) on debuggable builds.
 * <p>
 * Coverage files are written to <b>both</b> external storage
 * ({@link Context#getExternalFilesDir}) and internal storage
 * ({@link Context#getFilesDir}). External storage is preferred for extraction
 * because it does not require {@code run-as}, which can be unreliable on some
 * emulator images (especially API 30+).
 * <p>
 * Uses {@link #goAsync()} so that the dump runs on a background thread,
 * preventing the main-thread from blocking delivery when the UI is busy with
 * heavy view rendering (e.g. custom chart views on a software-rendered
 * emulator).
 * <p>
 * In addition to the {@code .ec} coverage file, the receiver always writes a
 * small {@code .status} file (same base name) that the collection script can
 * check. It also logs structured status messages with the tag
 * {@code CoverageBroadcastReceiver} and prefix {@code COVERAGE_RESULT:} so the
 * script can check receiver status via logcat without relying on file access.
 * <p>
 * Triggered via: adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE
 *                --es coverageFile coverage.ec
 */
public class CoverageBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CoverageBroadcastReceiver";
    private static final String DEFAULT_COVERAGE_FILENAME = "coverage.ec";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "COVERAGE_RECEIVER_ALIVE");
        final PendingResult pendingResult = goAsync();

        String coverageFileName = intent.getStringExtra("coverageFile");
        if (coverageFileName == null || coverageFileName.isEmpty()) {
            coverageFileName = DEFAULT_COVERAGE_FILENAME;
        }
        Log.i(TAG, "COVERAGE_STARTED:" + coverageFileName);

        File internalDir = context.getFilesDir();
        File externalDir = context.getExternalFilesDir(null);

        if (internalDir == null && externalDir == null) {
            Log.e(TAG, "No files directory available");
            Log.i(TAG, "COVERAGE_RESULT:ERROR:no_files_directory");
            pendingResult.finish();
            return;
        }

        File internalCoverage = internalDir != null ? new File(internalDir, coverageFileName) : null;
        File internalStatus = internalDir != null ? statusFileFor(internalDir, coverageFileName) : null;
        File externalCoverage = externalDir != null ? new File(externalDir, coverageFileName) : null;
        File externalStatus = externalDir != null ? statusFileFor(externalDir, coverageFileName) : null;

        final File fInternalCoverage = internalCoverage;
        final File fInternalStatus = internalStatus;
        final File fExternalCoverage = externalCoverage;
        final File fExternalStatus = externalStatus;
        new Thread(() -> {
            try {
                dumpCoverage(fInternalCoverage, fInternalStatus, fExternalCoverage, fExternalStatus);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private void dumpCoverage(File internalCoverage, File internalStatus,
                              File externalCoverage, File externalStatus) {
        try {
            // AGP uses offline instrumentation, so coverage data is stored in
            // org.jacoco.agent.rt.internal.Offline, not accessible via RT.getAgent().
            // Use reflection to avoid a compile-time dependency on the JaCoCo agent
            // JAR, which may not be on the javac classpath in all AGP versions.
            // Debug builds do not run R8/ProGuard, so the Offline class is kept in
            // the DEX via the debugImplementation dependency regardless.
            Class<?> offlineClass = Class.forName("org.jacoco.agent.rt.internal.Offline");
            Method getExecutionData = offlineClass.getMethod("getExecutionData", boolean.class);
            byte[] data = (byte[]) getExecutionData.invoke(null, false);

            if (data == null || data.length == 0) {
                String msg = "JaCoCo returned empty execution data — bytecode may not be instrumented";
                Log.w(TAG, msg);
                writeStatus(internalStatus, "EMPTY: " + msg);
                writeStatus(externalStatus, "EMPTY: " + msg);
                Log.i(TAG, "COVERAGE_RESULT:EMPTY");
                return;
            }

            writeCoverageData(internalCoverage, data);
            writeCoverageData(externalCoverage, data);

            String statusMsg = "OK: " + data.length + " bytes";
            writeStatus(internalStatus, statusMsg);
            writeStatus(externalStatus, statusMsg);
            Log.i(TAG, "COVERAGE_RESULT:OK:" + data.length);
            Log.d(TAG, "Coverage data written (" + data.length + " bytes)");
        } catch (ClassNotFoundException e) {
            reportError(internalStatus, externalStatus, "NO_CLASS",
                    "JaCoCo Offline class not available — app may not be instrumented");
        } catch (NoClassDefFoundError e) {
            reportError(internalStatus, externalStatus, "NO_CLASS",
                    "JaCoCo Offline class not available — app may not be instrumented");
        } catch (IOException e) {
            reportError(internalStatus, externalStatus, "IO_ERROR",
                    "Failed to write coverage data: " + e.getMessage());
        } catch (Exception e) {
            reportError(internalStatus, externalStatus, "UNEXPECTED",
                    "Unexpected error during coverage dump: " + e.getMessage());
        }
    }

    private void reportError(File internalStatus, File externalStatus, String code, String msg) {
        Log.w(TAG, msg);
        String status = code + ": " + msg;
        writeStatus(internalStatus, status);
        writeStatus(externalStatus, status);
        Log.i(TAG, "COVERAGE_RESULT:" + code);
    }

    private static void writeCoverageData(File file, byte[] data) throws IOException {
        if (file == null) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(data);
            out.flush();
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
