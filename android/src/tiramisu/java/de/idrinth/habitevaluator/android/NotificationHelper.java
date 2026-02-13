package de.idrinth.habitevaluator.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

/**
 * Tiramisu flavor notification helper (API 33+).
 * NotificationChannel is created unconditionally and the
 * POST_NOTIFICATIONS permission is always handled at runtime.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "habit_evaluator_reminders";

    private NotificationHelper() {
    }

    public static void ensureNotificationChannel(Context context) {
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

    public static NotificationPermissionHandler permissionHandler() {
        return new Api33PermissionHandler();
    }
}
