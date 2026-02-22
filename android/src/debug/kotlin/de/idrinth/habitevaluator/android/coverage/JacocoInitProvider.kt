package de.idrinth.habitevaluator.android.coverage

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * No-op ContentProvider that configures the JaCoCo agent before any
 * instrumented class is loaded.
 *
 * ContentProviders are instantiated before any Activity, so the
 * static initializer here runs early enough to configure the agent
 * via the system property that JaCoCo's ConfigLoader reads.
 * Setting output=none prevents the agent from opening any file;
 * coverage data is collected later by CoverageBroadcastReceiver via reflection.
 */
class JacocoInitProvider : ContentProvider() {

    companion object {
        init {
            System.setProperty("jacoco-agent.output", "none")
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}
