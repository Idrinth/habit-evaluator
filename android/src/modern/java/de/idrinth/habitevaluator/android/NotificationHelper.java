package de.idrinth.habitevaluator.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/**
 * Modern flavor notification helper.
 * NotificationChannel is created unconditionally since the modern
 * flavor guarantees API 26+.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "habit_evaluator_reminders";

    private NotificationHelper() {
    }

    /**
     * Creates the notification channel. No API check needed because
     * the modern flavor requires API 26+ where NotificationChannel
     * is always available.
     */
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
