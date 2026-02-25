package de.idrinth.habitevaluator.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import de.idrinth.habitevaluator.android.persistence.AppDatabase
import de.idrinth.habitevaluator.android.persistence.RoomWeekPlannerSlotRepository
import de.idrinth.habitevaluator.shared.util.ReminderScheduleCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderScheduler {

    private const val SLEEP_REMINDER_REQUEST_CODE = 9001
    private const val DIARY_REMINDER_REQUEST_CODE = 9002
    private const val EMOTION_REMINDER_BASE_REQUEST_CODE = 9100
    private const val PLANNER_REMINDER_BASE_REQUEST_CODE = 9200

    fun rescheduleAll(context: Context) {
        val prefs = context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE)
        scheduleSleepReminder(context, prefs)
        scheduleDiaryReminder(context, prefs)
        scheduleEmotionReminders(context, prefs)
        schedulePlannerReminders(context)
    }

    private fun scheduleSleepReminder(context: Context, prefs: SharedPreferences) {
        val enabled = prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pi = createPendingIntent(context, SLEEP_REMINDER_REQUEST_CODE, ReminderReceiver.TYPE_SLEEP)

        if (!enabled || alarmManager == null) {
            alarmManager?.cancel(pi)
            return
        }

        val time = prefs.getString(
            SettingsConstants.KEY_SLEEP_REMINDER_TIME,
            SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME
        ) ?: SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME
        val hm = ReminderScheduleCalculator.parseTime(time)
        scheduleDaily(alarmManager, pi, hm[0], hm[1])
    }

    private fun scheduleDiaryReminder(context: Context, prefs: SharedPreferences) {
        val enabled = prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pi = createPendingIntent(context, DIARY_REMINDER_REQUEST_CODE, ReminderReceiver.TYPE_DIARY)

        if (!enabled || alarmManager == null) {
            alarmManager?.cancel(pi)
            return
        }

        val time = prefs.getString(
            SettingsConstants.KEY_DIARY_REMINDER_TIME,
            SettingsConstants.DEFAULT_DIARY_REMINDER_TIME
        ) ?: SettingsConstants.DEFAULT_DIARY_REMINDER_TIME
        val hm = ReminderScheduleCalculator.parseTime(time)
        scheduleDaily(alarmManager, pi, hm[0], hm[1])
    }

    private fun scheduleEmotionReminders(context: Context, prefs: SharedPreferences) {
        val enabled = prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)
        val count = prefs.getInt(
            SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        // Cancel all possible emotion reminder slots (up to max 10)
        for (i in 0 until 10) {
            val pi = createPendingIntent(context, EMOTION_REMINDER_BASE_REQUEST_CODE + i, ReminderReceiver.TYPE_EMOTION)
            alarmManager?.cancel(pi)
        }

        if (!enabled || alarmManager == null) return

        val startStr = prefs.getString(
            SettingsConstants.KEY_WAKING_HOURS_START,
            SettingsConstants.DEFAULT_WAKING_HOURS_START
        ) ?: SettingsConstants.DEFAULT_WAKING_HOURS_START
        val endStr = prefs.getString(
            SettingsConstants.KEY_WAKING_HOURS_END,
            SettingsConstants.DEFAULT_WAKING_HOURS_END
        ) ?: SettingsConstants.DEFAULT_WAKING_HOURS_END

        val times = ReminderScheduleCalculator.calculateEmotionReminderTimes(startStr, endStr, count)
        times.forEachIndexed { i, time ->
            val hm = ReminderScheduleCalculator.parseTime(time)
            val pi = createPendingIntent(context, EMOTION_REMINDER_BASE_REQUEST_CODE + i, ReminderReceiver.TYPE_EMOTION)
            scheduleDaily(alarmManager, pi, hm[0], hm[1])
        }
    }

    private fun scheduleDaily(alarmManager: AlarmManager, pi: PendingIntent, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi
        )
    }

    private fun createPendingIntent(context: Context, requestCode: Int, type: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Schedules hourly alarms for each configured planner slot.
     * Each slot gets one daily alarm at its starting hour.
     * Slots are loaded from the database asynchronously.
     */
    fun schedulePlannerReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return

        // Cancel all previous planner alarms (up to 168 = 7 days * 24 hours)
        for (i in 0 until MAX_PLANNER_SLOTS) {
            val pi = createPlannerPendingIntent(context, PLANNER_REMINDER_BASE_REQUEST_CODE + i, 0, 0)
            alarmManager.cancel(pi)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val slotRepo = RoomWeekPlannerSlotRepository(db.dayPlannerDao())
                val slots = slotRepo.findAll()

                slots.forEachIndexed { index, slot ->
                    if (index >= MAX_PLANNER_SLOTS) return@forEachIndexed
                    if (slot.groups == null || slot.groups.isEmpty()) return@forEachIndexed

                    val pi = createPlannerPendingIntent(
                        context,
                        PLANNER_REMINDER_BASE_REQUEST_CODE + index,
                        slot.dayOfWeek,
                        slot.hour
                    )

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, slot.hour)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        // Advance to the correct day of week (ISO: 1=Mon..7=Sun)
                        val targetCalDay = if (slot.dayOfWeek == 7) Calendar.SUNDAY
                            else slot.dayOfWeek + 1 // Calendar: Sun=1,Mon=2..Sat=7
                        while (get(Calendar.DAY_OF_WEEK) != targetCalDay) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        // If this time already passed today, advance by a week
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.WEEK_OF_YEAR, 1)
                        }
                    }

                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        7 * AlarmManager.INTERVAL_DAY,
                        pi
                    )
                }
            } catch (_: Exception) {
                // Silently fail if database is not available yet
            }
        }
    }

    private fun createPlannerPendingIntent(
        context: Context,
        requestCode: Int,
        dayOfWeek: Int,
        hour: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_PLANNER)
            putExtra(ReminderReceiver.EXTRA_PLANNER_DAY, dayOfWeek)
            putExtra(ReminderReceiver.EXTRA_PLANNER_HOUR, hour)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private const val MAX_PLANNER_SLOTS = 168
}
