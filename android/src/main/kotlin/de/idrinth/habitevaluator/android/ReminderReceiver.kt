package de.idrinth.habitevaluator.android

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.idrinth.habitevaluator.android.persistence.AppDatabase
import de.idrinth.habitevaluator.android.persistence.RoomPlannerActivityRepository
import de.idrinth.habitevaluator.android.persistence.RoomWeekPlannerSlotRepository
import de.idrinth.habitevaluator.shared.service.DayPlannerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER = "de.idrinth.habitevaluator.REMINDER"
        const val EXTRA_TYPE = "reminder_type"
        const val EXTRA_PLANNER_DAY = "planner_day_of_week"
        const val EXTRA_PLANNER_HOUR = "planner_hour"
        const val TYPE_SLEEP = "sleep"
        const val TYPE_DIARY = "diary"
        const val TYPE_GRATITUDE = "gratitude"
        const val TYPE_EMOTION = "emotion"
        const val TYPE_PLANNER = "planner"

        private const val NOTIFICATION_SLEEP = 1001
        private const val NOTIFICATION_DIARY = 1002
        private const val NOTIFICATION_GRATITUDE = 1004
        private const val NOTIFICATION_EMOTION = 1003
        private const val NOTIFICATION_PLANNER_BASE = 2000
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            ReminderScheduler.rescheduleAll(context)
            return
        }

        if (ACTION_REMINDER != intent.action) return

        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        NotificationHelper.ensureNotificationChannel(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (type) {
                    TYPE_SLEEP -> {
                        if (!hasSleepEntryForToday(context)) {
                            showNotification(
                                context, NOTIFICATION_SLEEP,
                                context.getString(R.string.reminder_sleep_notification_title),
                                context.getString(R.string.reminder_sleep_notification_text),
                                NotificationHelper.CHANNEL_ID_INPUT_REMINDERS
                            )
                        }
                    }
                    TYPE_DIARY -> {
                        if (!hasDiaryEntryForToday(context)) {
                            showNotification(
                                context, NOTIFICATION_DIARY,
                                context.getString(R.string.reminder_diary_notification_title),
                                context.getString(R.string.reminder_diary_notification_text),
                                NotificationHelper.CHANNEL_ID_INPUT_REMINDERS
                            )
                        }
                    }
                    TYPE_GRATITUDE -> {
                        showNotification(
                            context, NOTIFICATION_GRATITUDE,
                            context.getString(R.string.reminder_gratitude_notification_title),
                            context.getString(R.string.reminder_gratitude_notification_text),
                            NotificationHelper.CHANNEL_ID_INPUT_REMINDERS
                        )
                    }
                    TYPE_EMOTION -> {
                        if (!hasEnoughEmotionEntriesForToday(context)) {
                            showNotification(
                                context, NOTIFICATION_EMOTION,
                                context.getString(R.string.reminder_emotion_notification_title),
                                context.getString(R.string.reminder_emotion_notification_text),
                                NotificationHelper.CHANNEL_ID_EMOTION
                            )
                        }
                    }
                    TYPE_PLANNER -> {
                        val dayOfWeek = intent.getIntExtra(EXTRA_PLANNER_DAY, 0)
                        val hour = intent.getIntExtra(EXTRA_PLANNER_HOUR, 0)
                        showPlannerNotification(context, dayOfWeek, hour)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun hasSleepEntryForToday(context: Context): Boolean = try {
        val db = AppDatabase.getInstance(context)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entries = db.sleepEntryDao().findAll()
        entries.any { it.date == today }
    } catch (_: Exception) {
        false
    }

    private suspend fun hasDiaryEntryForToday(context: Context): Boolean = try {
        val db = AppDatabase.getInstance(context)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entries = db.diaryDao().findAllEntries()
        entries.any { it.eventDate == today }
    } catch (_: Exception) {
        false
    }

    private suspend fun hasEnoughEmotionEntriesForToday(context: Context): Boolean = try {
        val prefs = context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val requiredCount = prefs.getInt(
            SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT
        )
        val db = AppDatabase.getInstance(context)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entries = db.emotionDao().findAllEntries()
        entries.count { it.recordedAt.startsWith(today) } >= requiredCount
    } catch (_: Exception) {
        false
    }

    private suspend fun showPlannerNotification(context: Context, dayOfWeek: Int, hour: Int) {
        try {
            val db = AppDatabase.getInstance(context)
            val slotRepo = RoomWeekPlannerSlotRepository(db.dayPlannerDao())
            val activityRepo = RoomPlannerActivityRepository(db.dayPlannerDao())

            // Find the first user who has slots for this day/hour
            val allSlots = slotRepo.findAll()
            val matchingSlots = allSlots.filter { it.dayOfWeek == dayOfWeek && it.coversHour(hour) }
            if (matchingSlots.isEmpty()) return

            val userId = matchingSlots.first().user?.id ?: return
            val userSlots = matchingSlots.filter { it.user?.id == userId }
            val allActivities = activityRepo.findByUserId(userId)

            val service = DayPlannerService()
            val suggested = service.suggestActivity(userSlots, allActivities) ?: return

            // Find the group that was chosen (the one both the slot and activity share)
            val slotGroups = userSlots.flatMap { it.groups ?: emptySet() }.toSet()
            val activityGroups = suggested.groups ?: emptySet()
            val sharedGroup = slotGroups.intersect(activityGroups).firstOrNull() ?: return
            val slotId = userSlots.first().id

            val notificationId = NOTIFICATION_PLANNER_BASE + dayOfWeek * 100 + hour

            // Schedule timeout auto-deny
            PlannerConfirmationReceiver.scheduleTimeout(
                context, notificationId, slotId, suggested.id, sharedGroup.id, userId
            )

            val confirmIntent = Intent(context, PlannerConfirmationReceiver::class.java).apply {
                action = PlannerConfirmationReceiver.ACTION_CONFIRM
                putExtra(PlannerConfirmationReceiver.EXTRA_SLOT_ID, slotId)
                putExtra(PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID, suggested.id)
                putExtra(PlannerConfirmationReceiver.EXTRA_GROUP_ID, sharedGroup.id)
                putExtra(PlannerConfirmationReceiver.EXTRA_USER_ID, userId)
                putExtra(PlannerConfirmationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val confirmPi = PendingIntent.getBroadcast(
                context, notificationId + 10000, confirmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val denyIntent = Intent(context, PlannerConfirmationReceiver::class.java).apply {
                action = PlannerConfirmationReceiver.ACTION_DENY
                putExtra(PlannerConfirmationReceiver.EXTRA_SLOT_ID, slotId)
                putExtra(PlannerConfirmationReceiver.EXTRA_ACTIVITY_ID, suggested.id)
                putExtra(PlannerConfirmationReceiver.EXTRA_GROUP_ID, sharedGroup.id)
                putExtra(PlannerConfirmationReceiver.EXTRA_USER_ID, userId)
                putExtra(PlannerConfirmationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val denyPi = PendingIntent.getBroadcast(
                context, notificationId + 20000, denyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentPi = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = context.getString(R.string.planner_notification_title)
            val text = context.getString(R.string.planner_notification_text, suggested.name)

            val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_DAY_PLANNER)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentPi)
                .setAutoCancel(false)
                .addAction(0, context.getString(R.string.planner_notification_confirm), confirmPi)
                .addAction(0, context.getString(R.string.planner_notification_deny), denyPi)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(notificationId, builder.build())
        } catch (_: Exception) {
            // Silently fail if database is unavailable
        }
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, text: String, channelId: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(notificationId, builder.build())
    }
}
