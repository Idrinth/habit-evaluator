package de.idrinth.habitevaluator.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * Legacy flavor notification helper.
 * Guards NotificationChannel creation behind an API 26 check since
 * the legacy flavor supports devices running API 21+.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "habit_evaluator_reminders";

    private static final String PERMISSION_POST_NOTIFICATIONS =
            "android.permission.POST_NOTIFICATIONS";

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
     * Checks whether the app has permission to post notifications.
     * On API &lt; 33 this always returns {@code true} because runtime
     * permission is not required.
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(context, PERMISSION_POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Returns the POST_NOTIFICATIONS permission string when running
     * on API 33+, or {@code null} on older devices where runtime
     * permission is not needed.
     */
    public static String notificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            return PERMISSION_POST_NOTIFICATIONS;
        }
        return null;
    }
}
