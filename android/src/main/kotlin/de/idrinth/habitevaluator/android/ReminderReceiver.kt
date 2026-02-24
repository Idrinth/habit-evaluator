package de.idrinth.habitevaluator.android

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.idrinth.habitevaluator.android.persistence.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER = "de.idrinth.habitevaluator.REMINDER"
        const val EXTRA_TYPE = "reminder_type"
        const val TYPE_SLEEP = "sleep"
        const val TYPE_DIARY = "diary"
        const val TYPE_EMOTION = "emotion"

        private const val NOTIFICATION_SLEEP = 1001
        private const val NOTIFICATION_DIARY = 1002
        private const val NOTIFICATION_EMOTION = 1003
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
