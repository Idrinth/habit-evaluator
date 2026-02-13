package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * POST_NOTIFICATIONS permission handler for API 33+ (Tiramisu).
 * Only instantiated on devices running Android 13 or newer.
 */
@RequiresApi(33)
final class Api33PermissionHandler implements NotificationPermissionHandler {

    private static final String PERMISSION_POST_NOTIFICATIONS =
            "android.permission.POST_NOTIFICATIONS";

    @Override
    public boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, PERMISSION_POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public String permissionName() {
        return PERMISSION_POST_NOTIFICATIONS;
    }
}
