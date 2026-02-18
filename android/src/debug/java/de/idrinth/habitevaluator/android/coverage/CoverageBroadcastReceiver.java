package de.idrinth.habitevaluator.android.coverage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;

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
 * emulator). All file I/O (including heartbeat status writes) is performed on
 * the background thread to minimise the time spent in {@code onReceive()} on
 * the main thread.
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
            setResult(pendingResult, Activity.RESULT_CANCELED, "ERROR:no_files_directory");
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
                // Write a heartbeat file so the collection script can detect
                // that onReceive was dispatched, even if the JaCoCo dump fails
                // later.  Done on the background thread to minimise the time
                // spent in onReceive() on the main thread.
                writeStatus(fInternalStatus, "STARTED");
                writeStatus(fExternalStatus, "STARTED");
                dumpCoverage(context, pendingResult, fInternalCoverage, fInternalStatus,
                        fExternalCoverage, fExternalStatus);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private void dumpCoverage(Context context, PendingResult pendingResult,
                              File internalCoverage, File internalStatus,
                              File externalCoverage, File externalStatus) {
        try {
            // JaCoCo offline instrumentation stores coverage data in the
            // Agent singleton, which is initialized when the first
            // instrumented class is loaded (via Offline.getProbes()).
            // The public API to retrieve execution data is through
            // RT.getAgent() which returns an IAgent with
            // getExecutionData(boolean).
            byte[] data = getExecutionData(context);

            if (data == null || data.length == 0) {
                String msg = "JaCoCo returned empty execution data — bytecode may not be instrumented";
                Log.w(TAG, msg);
                writeStatus(internalStatus, "EMPTY: " + msg);
                writeStatus(externalStatus, "EMPTY: " + msg);
                Log.i(TAG, "COVERAGE_RESULT:EMPTY");
                setResult(pendingResult, Activity.RESULT_CANCELED, "EMPTY");
                return;
            }

            writeCoverageData(internalCoverage, data);
            writeCoverageData(externalCoverage, data);

            String statusMsg = "OK: " + data.length + " bytes";
            writeStatus(internalStatus, statusMsg);
            writeStatus(externalStatus, statusMsg);
            Log.i(TAG, "COVERAGE_RESULT:OK:" + data.length);
            Log.d(TAG, "Coverage data written (" + data.length + " bytes)");
            setResult(pendingResult, Activity.RESULT_OK, "OK:" + data.length);
        } catch (ClassNotFoundException e) {
            reportError(pendingResult, internalStatus, externalStatus, "NO_CLASS",
                    "ClassNotFoundException: " + e.getMessage());
        } catch (NoClassDefFoundError e) {
            String cause = e.getCause() != null ? e.getCause().toString() : "no cause";
            reportError(pendingResult, internalStatus, externalStatus, "NO_CLASS",
                    "NoClassDefFoundError: JaCoCo agent class failed to initialize — "
                    + cause);
        } catch (IOException e) {
            reportError(pendingResult, internalStatus, externalStatus, "IO_ERROR",
                    "Failed to write coverage data: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during coverage dump", e);
            reportError(pendingResult, internalStatus, externalStatus, "UNEXPECTED",
                    "Unexpected error during coverage dump: " + e);
        }
    }

    /**
     * Retrieves JaCoCo execution data using the public RT/IAgent API.
     * <p>
     * When offline instrumentation is active, loading any instrumented class
     * triggers {@code Offline.getProbes()} which initialises the JaCoCo
     * {@code Agent} singleton. The public {@code RT.getAgent()} method
     * returns this singleton (as {@code IAgent}), whose
     * {@code getExecutionData(boolean)} serialises the accumulated probe
     * data into the JaCoCo {@code .exec}/{@code .ec} binary format.
     * <p>
     * If the public API is unavailable (e.g. class-loading issues on some
     * Android versions), this method falls back to locating the shaded
     * {@code Agent} class via the {@code Offline} class's package name and
     * calling {@code getInstance().getExecutionData(false)} directly.
     */
    private byte[] getExecutionData(Context context) throws Exception {
        // Primary: use the public JaCoCo RT API (not shaded).
        // RT is at org.jacoco.agent.rt.RT and IAgent is at
        // org.jacoco.agent.rt.IAgent — neither is affected by
        // the internal package shading.
        try {
            Class<?> rtClass = Class.forName("org.jacoco.agent.rt.RT");
            Object agent = rtClass.getMethod("getAgent").invoke(null);
            Class<?> iAgentClass = Class.forName("org.jacoco.agent.rt.IAgent");
            Method getExecutionData = iAgentClass.getMethod("getExecutionData", boolean.class);
            byte[] data = (byte[]) getExecutionData.invoke(agent, false);
            Log.d(TAG, "Coverage data retrieved via RT.getAgent()");
            return data;
        } catch (Exception e) {
            Log.w(TAG, "RT.getAgent() failed (" + e.getMessage()
                    + "), trying shaded Agent fallback");
        }

        // Fallback: find the shaded Agent class via the Offline class's
        // package.  The Agent class lives in the same shaded package
        // (org.jacoco.agent.rt.internal_<hash>) and exposes
        // getInstance() / getExecutionData(boolean).
        Class<?> offlineClass = findOfflineClass(context);
        String agentClassName = offlineClass.getName().replace(".Offline", ".Agent");
        Log.d(TAG, "Trying shaded Agent class: " + agentClassName);
        Class<?> agentClass = Class.forName(agentClassName, true, context.getClassLoader());
        Object agent = agentClass.getMethod("getInstance").invoke(null);
        Method getExecutionData = agent.getClass().getMethod("getExecutionData", boolean.class);
        byte[] data = (byte[]) getExecutionData.invoke(agent, false);
        Log.d(TAG, "Coverage data retrieved via shaded Agent class");
        return data;
    }

    /**
     * Locates the JaCoCo {@code Offline} class in the APK's DEX.
     * <p>
     * JaCoCo's published {@code runtime} classifier JAR shades the internal
     * package to {@code org.jacoco.agent.rt.internal_<hash>} where the hash
     * is derived from the build's Git commit ID and changes between versions.
     * The Ant {@code instrument} task produces bytecode referencing the same
     * shaded name, so the instrumented code and runtime match — but we cannot
     * hardcode the class name for reflective access.
     * <p>
     * This method first tries the canonical (unshaded) name, then falls back
     * to scanning the DEX entries for any class matching the shaded pattern.
     */
    @SuppressWarnings("deprecation")
    private static Class<?> findOfflineClass(Context context) throws ClassNotFoundException {
        // Try the canonical (unshaded) name first — works if the runtime JAR
        // is ever published without shading or if a future version changes.
        try {
            return Class.forName("org.jacoco.agent.rt.internal.Offline");
        } catch (ClassNotFoundException ignored) {
            // Expected for standard JaCoCo releases that shade internal classes.
        }

        // Scan the APK's DEX entries for the shaded variant.
        // DexFile is deprecated since API 26 but remains functional through
        // API 35.  This is debug-only code, so using a deprecated API is
        // acceptable.
        try {
            dalvik.system.DexFile dexFile = new dalvik.system.DexFile(
                    context.getPackageCodePath());
            try {
                Enumeration<String> entries = dexFile.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement();
                    if (name.startsWith("org.jacoco.agent.rt.internal")
                            && name.endsWith(".Offline")) {
                        Log.d(TAG, "Found shaded JaCoCo Offline class: " + name);
                        return Class.forName(name, true, context.getClassLoader());
                    }
                }
            } finally {
                dexFile.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to scan DEX for JaCoCo Offline class: "
                    + e.getMessage());
        }

        throw new ClassNotFoundException(
                "JaCoCo Offline class not found — neither the unshaded "
                + "(org.jacoco.agent.rt.internal.Offline) nor a shaded variant "
                + "(org.jacoco.agent.rt.internal_<hash>.Offline) was detected in DEX");
    }

    private void reportError(PendingResult pendingResult,
                             File internalStatus, File externalStatus,
                             String code, String msg) {
        Log.w(TAG, msg);
        String status = code + ": " + msg;
        writeStatus(internalStatus, status);
        writeStatus(externalStatus, status);
        Log.i(TAG, "COVERAGE_RESULT:" + code + ":" + msg);
        setResult(pendingResult, Activity.RESULT_CANCELED, code);
    }

    private static void setResult(PendingResult pendingResult, int code, String data) {
        try {
            pendingResult.setResultCode(code);
            pendingResult.setResultData(data);
        } catch (Exception e) {
            Log.w(TAG, "Failed to set broadcast result: " + e.getMessage());
        }
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
