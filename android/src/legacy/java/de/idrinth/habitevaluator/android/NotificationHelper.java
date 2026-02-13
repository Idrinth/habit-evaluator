package de.idrinth.habitevaluator.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/**
 * Legacy flavor notification helper.
 * Guards NotificationChannel creation behind an API 26 check since
 * the legacy flavor supports devices running API 21+.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "habit_evaluator_reminders";

    private NotificationHelper() {
    }

    /**
     * Creates the notification channel if running on API 26+.
     * On older devices this is a no-op since notification channels
     * do not exist.
     */
    public static void ensureNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.reminder_channel_description));
            NotificationManager manager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Returns the appropriate {@link NotificationPermissionHandler}
     * for the current API level.
     */
    public static NotificationPermissionHandler permissionHandler() {
        if (Build.VERSION.SDK_INT >= 33) {
            return new Api33PermissionHandler();
        }
        return new PreApi33PermissionHandler();
    }
}
