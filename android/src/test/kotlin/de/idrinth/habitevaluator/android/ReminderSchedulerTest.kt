package de.idrinth.habitevaluator.android

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

class ReminderSchedulerTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    @BeforeEach
    fun setUp() {
        context = mock(Context::class.java)
        prefs = mock(SharedPreferences::class.java)
        `when`(context.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE))
            .thenReturn(prefs)
    }

    @Test
    fun testRescheduleAllWithAllRemindersDisabled() {
        `when`(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3)

        // AlarmManager is null (system service returns null)
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(null)

        ReminderScheduler.rescheduleAll(context)
        // Should not throw when AlarmManager is null
    }

    @Test
    fun testRescheduleAllWithNullAlarmManager() {
        `when`(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3)
        `when`(prefs.getString(SettingsConstants.KEY_SLEEP_REMINDER_TIME,
            SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME))
            .thenReturn(SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME)
        `when`(prefs.getString(SettingsConstants.KEY_DIARY_REMINDER_TIME,
            SettingsConstants.DEFAULT_DIARY_REMINDER_TIME))
            .thenReturn(SettingsConstants.DEFAULT_DIARY_REMINDER_TIME)
        `when`(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_START,
            SettingsConstants.DEFAULT_WAKING_HOURS_START))
            .thenReturn(SettingsConstants.DEFAULT_WAKING_HOURS_START)
        `when`(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_END,
            SettingsConstants.DEFAULT_WAKING_HOURS_END))
            .thenReturn(SettingsConstants.DEFAULT_WAKING_HOURS_END)

        // AlarmManager is null
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(null)

        ReminderScheduler.rescheduleAll(context)
        // Should not throw when AlarmManager is null
    }

    @Test
    fun testRescheduleAllWithMockedAlarmManager() {
        val alarmManager = mock(AlarmManager::class.java)
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager)

        `when`(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3)
        `when`(prefs.getString(SettingsConstants.KEY_SLEEP_REMINDER_TIME,
            SettingsConstants.DEFAULT_SLEEP_REMINDER_TIME))
            .thenReturn("22:00")
        `when`(prefs.getString(SettingsConstants.KEY_DIARY_REMINDER_TIME,
            SettingsConstants.DEFAULT_DIARY_REMINDER_TIME))
            .thenReturn("20:00")

        ReminderScheduler.rescheduleAll(context)

        // Verify alarm was scheduled for both sleep and diary
        verify(alarmManager, times(2)).setInexactRepeating(
            eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(AlarmManager.INTERVAL_DAY), any())
    }

    @Test
    fun testRescheduleAllCancelsDisabledReminders() {
        val alarmManager = mock(AlarmManager::class.java)
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager)

        `when`(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3)

        ReminderScheduler.rescheduleAll(context)

        // When disabled, sleep + diary are cancelled (1 each) + 10 emotion slots = 12 cancels
        // PendingIntent.getBroadcast returns null with returnDefaultValues, so cancel is called with null
        verify(alarmManager, times(12)).cancel(isNull<android.app.PendingIntent>())
    }

    @Test
    fun testRescheduleAllSchedulesEmotionReminders() {
        val alarmManager = mock(AlarmManager::class.java)
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager)

        `when`(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3)
        `when`(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_START,
            SettingsConstants.DEFAULT_WAKING_HOURS_START))
            .thenReturn("07:00")
        `when`(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_END,
            SettingsConstants.DEFAULT_WAKING_HOURS_END))
            .thenReturn("22:00")

        ReminderScheduler.rescheduleAll(context)

        // 10 emotion slots cancelled + 3 emotion reminders scheduled
        verify(alarmManager, times(3)).setInexactRepeating(
            eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(AlarmManager.INTERVAL_DAY), any())
    }

    @Test
    fun testRescheduleAllWithSingleEmotionReminder() {
        val alarmManager = mock(AlarmManager::class.java)
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager)

        `when`(prefs.getBoolean(SettingsConstants.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false)
        `when`(prefs.getBoolean(SettingsConstants.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(true)
        `when`(prefs.getInt(SettingsConstants.KEY_EMOTION_REMINDER_COUNT,
            SettingsConstants.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(1)
        `when`(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_START,
            SettingsConstants.DEFAULT_WAKING_HOURS_START))
            .thenReturn("08:00")
        `when`(prefs.getString(SettingsConstants.KEY_WAKING_HOURS_END,
            SettingsConstants.DEFAULT_WAKING_HOURS_END))
            .thenReturn("20:00")

        ReminderScheduler.rescheduleAll(context)

        // 1 emotion reminder scheduled
        verify(alarmManager, times(1)).setInexactRepeating(
            eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(AlarmManager.INTERVAL_DAY), any())
    }
}
