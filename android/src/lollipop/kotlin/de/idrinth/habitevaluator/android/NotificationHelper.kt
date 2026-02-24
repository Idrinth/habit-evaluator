package de.idrinth.habitevaluator.android

import android.content.Context

object NotificationHelper {
    const val CHANNEL_ID = "habit_evaluator_reminders"
    const val CHANNEL_ID_INPUT_REMINDERS = "habit_evaluator_input_reminders"
    const val CHANNEL_ID_EMOTION = "habit_evaluator_emotion"
    const val CHANNEL_ID_DAY_PLANNER = "habit_evaluator_day_planner"

    fun ensureNotificationChannel(context: Context) {
        // No-op: notification channels and groups were introduced in API 26 (Oreo).
    }

    fun permissionHandler(): NotificationPermissionHandler = PreApi33PermissionHandler()
}
