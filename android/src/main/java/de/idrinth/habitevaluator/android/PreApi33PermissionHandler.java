package de.idrinth.habitevaluator.android;

import android.content.Context;

/**
 * No-op implementation for devices below API 33.
 * Notifications are permitted without a runtime permission request.
 */
final class PreApi33PermissionHandler implements NotificationPermissionHandler {

    @Override
    public boolean hasPermission(Context context) {
        return true;
    }

    @Override
    public String permissionName() {
        return null;
    }
}
