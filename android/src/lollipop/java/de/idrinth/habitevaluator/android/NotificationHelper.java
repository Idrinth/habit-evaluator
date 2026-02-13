package de.idrinth.habitevaluator.android;

import android.content.Context;

/**
 * Lollipop flavor notification helper (API 21–25).
 * Notification channels do not exist before API 26, so channel
 * creation is a no-op. Devices running API 26+ use the oreo flavor.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "habit_evaluator_reminders";

    private NotificationHelper() {
    }

    public static void ensureNotificationChannel(Context context) {
        // No-op: notification channels were introduced in API 26 (Oreo).
    }

    public static NotificationPermissionHandler permissionHandler() {
        return new PreApi33PermissionHandler();
    }
}
