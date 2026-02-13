package de.idrinth.habitevaluator.android;

import android.content.Context;

/**
 * Abstraction for notification permission handling across API levels.
 * Pre-API 33 devices use a no-op implementation; API 33+ devices
 * use one that checks and requests the POST_NOTIFICATIONS permission.
 */
public interface NotificationPermissionHandler {

    /**
     * Returns {@code true} if the app has permission to post notifications.
     */
    boolean hasPermission(Context context);

    /**
     * Returns the permission string to pass to a permission request launcher,
     * or {@code null} if no runtime permission is needed.
     */
    String permissionName();
}
