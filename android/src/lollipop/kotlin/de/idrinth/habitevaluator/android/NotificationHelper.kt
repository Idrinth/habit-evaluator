package de.idrinth.habitevaluator.android

import android.content.Context

object NotificationHelper {
    const val CHANNEL_ID = "habit_evaluator_reminders"

    fun ensureNotificationChannel(context: Context) {
        // No-op: notification channels were introduced in API 26 (Oreo).
    }

    fun permissionHandler(): NotificationPermissionHandler = PreApi33PermissionHandler()
}
