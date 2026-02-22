package de.idrinth.habitevaluator.android.coverage

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Debug-only broadcast receiver that dumps JaCoCo execution data to the app's
 * files directory, accessible via `adb pull` (external) or
 * `adb exec-out run-as <package>` (internal) on debuggable builds.
 *
 * Triggered via: adb shell am broadcast -a de.idrinth.habitevaluator.android.DUMP_COVERAGE
 *                --es coverageFile coverage.ec
 */
class CoverageBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CoverageBroadcastReceiver"
        private const val DEFAULT_COVERAGE_FILENAME = "coverage.ec"

        private fun setResult(pendingResult: PendingResult, code: Int, data: String) {
            try {
                pendingResult.resultCode = code
                pendingResult.resultData = data
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set broadcast result: ${e.message}")
            }
        }

        private fun writeCoverageData(file: File?, data: ByteArray) {
            if (file == null) return
            file.parentFile?.let { if (!it.exists()) it.mkdirs() }
            FileOutputStream(file).use { out ->
                out.write(data)
                out.flush()
            }
        }

        private fun statusFileFor(dir: File, coverageFileName: String): File {
            val base = if (coverageFileName.endsWith(".ec"))
                coverageFileName.substring(0, coverageFileName.length - 3)
            else coverageFileName
            return File(dir, "$base.status")
        }

        private fun writeStatus(statusFile: File?, message: String) {
            if (statusFile == null) return
            try {
                FileOutputStream(statusFile).use { out ->
                    out.write(message.toByteArray(Charsets.UTF_8))
                    out.flush()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to write status file", e)
            }
        }

        @Suppress("deprecation")
        private fun findOfflineClass(context: Context): Class<*> {
            try {
                return Class.forName("org.jacoco.agent.rt.internal.Offline")
            } catch (_: ClassNotFoundException) {
                // Expected for standard JaCoCo releases that shade internal classes.
            }

            try {
                val dexFile = dalvik.system.DexFile(context.packageCodePath)
                try {
                    val entries = dexFile.entries()
                    while (entries.hasMoreElements()) {
                        val name = entries.nextElement()
                        if (name.startsWith("org.jacoco.agent.rt.internal") && name.endsWith(".Offline")) {
                            Log.d(TAG, "Found shaded JaCoCo Offline class: $name")
                            return Class.forName(name, true, context.classLoader)
                        }
                    }
                } finally {
                    dexFile.close()
                }
            } catch (e: IOException) {
                Log.w(TAG, "Failed to scan DEX for JaCoCo Offline class: ${e.message}")
            }

            throw ClassNotFoundException(
                "JaCoCo Offline class not found — neither the unshaded " +
                        "(org.jacoco.agent.rt.internal.Offline) nor a shaded variant " +
                        "(org.jacoco.agent.rt.internal_<hash>.Offline) was detected in DEX"
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "COVERAGE_RECEIVER_ALIVE")
        val pendingResult = goAsync()

        var coverageFileName = intent.getStringExtra("coverageFile")
        if (coverageFileName.isNullOrEmpty()) {
            coverageFileName = DEFAULT_COVERAGE_FILENAME
        }
        Log.i(TAG, "COVERAGE_STARTED:$coverageFileName")

        val internalDir = context.filesDir
        val externalDir = context.getExternalFilesDir(null)

        if (internalDir == null && externalDir == null) {
            Log.e(TAG, "No files directory available")
            Log.i(TAG, "COVERAGE_RESULT:ERROR:no_files_directory")
            setResult(pendingResult, Activity.RESULT_CANCELED, "ERROR:no_files_directory")
            pendingResult.finish()
            return
        }

        val internalCoverage = internalDir?.let { File(it, coverageFileName) }
        val internalStatus = internalDir?.let { statusFileFor(it, coverageFileName) }
        val externalCoverage = externalDir?.let { File(it, coverageFileName) }
        val externalStatus = externalDir?.let { statusFileFor(it, coverageFileName) }

        Thread {
            try {
                writeStatus(internalStatus, "STARTED")
                writeStatus(externalStatus, "STARTED")
                dumpCoverage(context, pendingResult, internalCoverage, internalStatus,
                    externalCoverage, externalStatus)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun dumpCoverage(
        context: Context, pendingResult: PendingResult,
        internalCoverage: File?, internalStatus: File?,
        externalCoverage: File?, externalStatus: File?
    ) {
        try {
            val data = getExecutionData(context)

            if (data == null || data.isEmpty()) {
                val msg = "JaCoCo returned empty execution data — bytecode may not be instrumented"
                Log.w(TAG, msg)
                writeStatus(internalStatus, "EMPTY: $msg")
                writeStatus(externalStatus, "EMPTY: $msg")
                Log.i(TAG, "COVERAGE_RESULT:EMPTY")
                setResult(pendingResult, Activity.RESULT_CANCELED, "EMPTY")
                return
            }

            writeCoverageData(internalCoverage, data)
            writeCoverageData(externalCoverage, data)

            val statusMsg = "OK: ${data.size} bytes"
            writeStatus(internalStatus, statusMsg)
            writeStatus(externalStatus, statusMsg)
            Log.i(TAG, "COVERAGE_RESULT:OK:${data.size}")
            Log.d(TAG, "Coverage data written (${data.size} bytes)")
            setResult(pendingResult, Activity.RESULT_OK, "OK:${data.size}")
        } catch (e: ClassNotFoundException) {
            reportError(pendingResult, internalStatus, externalStatus, "NO_CLASS",
                "ClassNotFoundException: ${e.message}")
        } catch (e: NoClassDefFoundError) {
            val cause = e.cause?.toString() ?: "no cause"
            reportError(pendingResult, internalStatus, externalStatus, "NO_CLASS",
                "NoClassDefFoundError: JaCoCo agent class failed to initialize — $cause")
        } catch (e: IOException) {
            reportError(pendingResult, internalStatus, externalStatus, "IO_ERROR",
                "Failed to write coverage data: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during coverage dump", e)
            reportError(pendingResult, internalStatus, externalStatus, "UNEXPECTED",
                "Unexpected error during coverage dump: $e")
        }
    }

    private fun getExecutionData(context: Context): ByteArray? {
        try {
            val rtClass = Class.forName("org.jacoco.agent.rt.RT")
            val agent = rtClass.getMethod("getAgent").invoke(null)
            val iAgentClass = Class.forName("org.jacoco.agent.rt.IAgent")
            val getExecutionData = iAgentClass.getMethod("getExecutionData", Boolean::class.javaPrimitiveType)
            val data = getExecutionData.invoke(agent, false) as ByteArray?
            Log.d(TAG, "Coverage data retrieved via RT.getAgent()")
            return data
        } catch (e: Exception) {
            Log.w(TAG, "RT.getAgent() failed (${e.message}), trying shaded Agent fallback")
        }

        val offlineClass = findOfflineClass(context)
        val agentClassName = offlineClass.name.replace(".Offline", ".Agent")
        Log.d(TAG, "Trying shaded Agent class: $agentClassName")
        val agentClass = Class.forName(agentClassName, true, context.classLoader)
        val agent = agentClass.getMethod("getInstance").invoke(null)
            ?: throw IllegalStateException("Shaded Agent.getInstance() returned null — agent not initialised")
        val getExecutionData = agent.javaClass.getMethod("getExecutionData", Boolean::class.javaPrimitiveType)
        val data = getExecutionData.invoke(agent, false) as ByteArray?
        Log.d(TAG, "Coverage data retrieved via shaded Agent class")
        return data
    }

    private fun reportError(
        pendingResult: PendingResult,
        internalStatus: File?, externalStatus: File?,
        code: String, msg: String
    ) {
        Log.w(TAG, msg)
        val status = "$code: $msg"
        writeStatus(internalStatus, status)
        writeStatus(externalStatus, status)
        Log.i(TAG, "COVERAGE_RESULT:$code:$msg")
        setResult(pendingResult, Activity.RESULT_CANCELED, code)
    }
}
