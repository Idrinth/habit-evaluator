package de.idrinth.habitevaluator.android.coverage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * No-op ContentProvider that configures the JaCoCo agent before any
 * instrumented class is loaded.
 * <p>
 * When the debug APK is built with offline JaCoCo instrumentation
 * ({@code -PmaestroCoverage}), every instrumented class triggers
 * JaCoCo agent initialization in its static initializer. By default
 * the agent tries to write {@code /jacoco.exec}, which fails on
 * Android's read-only root filesystem ({@code EROFS}).
 * <p>
 * ContentProviders are instantiated before any Activity, so the
 * static initializer here runs early enough to configure the agent
 * via the system property that JaCoCo's {@code ConfigLoader} reads.
 * Setting {@code output=none} prevents the agent from opening any
 * file; coverage data is collected later by
 * {@link CoverageBroadcastReceiver} via reflection.
 * <p>
 * This class lives in the {@code coverage} package, which is excluded
 * from JaCoCo instrumentation in {@code build.gradle}, avoiding a
 * circular initialization problem.
 */
public class JacocoInitProvider extends ContentProvider {

    static {
        System.setProperty("/jacoco-agent.properties", "output=none");
        System.setProperty("jacoco-agent.properties", "output=none");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }
}
