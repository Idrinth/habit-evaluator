package de.idrinth.habitevaluator.android

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {
    const val CHANNEL_ID = "habit_evaluator_reminders"
    const val CHANNEL_ID_INPUT_REMINDERS = "habit_evaluator_input_reminders"
    const val CHANNEL_ID_EMOTION = "habit_evaluator_emotion"
    const val CHANNEL_ID_DAY_PLANNER = "habit_evaluator_day_planner"

    private const val GROUP_ID_INPUT_REMINDERS = "group_input_reminders"
    private const val GROUP_ID_EMOTION = "group_emotion"
    private const val GROUP_ID_DAY_PLANNER = "group_day_planner"

    fun ensureNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        manager.createNotificationChannelGroups(listOf(
            NotificationChannelGroup(
                GROUP_ID_INPUT_REMINDERS,
                context.getString(R.string.notification_group_input_reminders)
            ),
            NotificationChannelGroup(
                GROUP_ID_EMOTION,
                context.getString(R.string.notification_group_emotion)
            ),
            NotificationChannelGroup(
                GROUP_ID_DAY_PLANNER,
                context.getString(R.string.notification_group_day_planner)
            )
        ))

        val inputRemindersChannel = NotificationChannel(
            CHANNEL_ID_INPUT_REMINDERS,
            context.getString(R.string.notification_channel_input_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_input_reminders_description)
            group = GROUP_ID_INPUT_REMINDERS
        }

        val emotionChannel = NotificationChannel(
            CHANNEL_ID_EMOTION,
            context.getString(R.string.notification_channel_emotion),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_emotion_description)
            group = GROUP_ID_EMOTION
        }

        val dayPlannerChannel = NotificationChannel(
            CHANNEL_ID_DAY_PLANNER,
            context.getString(R.string.notification_channel_day_planner),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_day_planner_description)
            group = GROUP_ID_DAY_PLANNER
        }

        manager.createNotificationChannels(listOf(
            inputRemindersChannel,
            emotionChannel,
            dayPlannerChannel
        ))

        manager.deleteNotificationChannel(CHANNEL_ID)
    }

    fun permissionHandler(): NotificationPermissionHandler = PreApi33PermissionHandler()
}
