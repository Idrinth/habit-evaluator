package de.idrinth.habitevaluator.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {
    const val CHANNEL_ID = "habit_evaluator_reminders"

    fun ensureNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.createNotificationChannel(channel)
    }

    fun permissionHandler(): NotificationPermissionHandler = Api33PermissionHandler()
}
