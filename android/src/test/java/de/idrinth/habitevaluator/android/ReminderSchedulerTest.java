package de.idrinth.habitevaluator.android;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ReminderSchedulerTest {

    private Context context;
    private SharedPreferences prefs;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        prefs = mock(SharedPreferences.class);
        when(context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE))
                .thenReturn(prefs);
    }

    @Test
    void testRescheduleAllWithAllRemindersDisabled() {
        when(prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3);

        // AlarmManager is null (system service returns null)
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(null);

        ReminderScheduler.rescheduleAll(context);
        // Should not throw when AlarmManager is null
    }

    @Test
    void testRescheduleAllWithNullAlarmManager() {
        when(prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3);
        when(prefs.getString(SettingsActivity.KEY_SLEEP_REMINDER_TIME,
                SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME))
                .thenReturn(SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        when(prefs.getString(SettingsActivity.KEY_DIARY_REMINDER_TIME,
                SettingsActivity.DEFAULT_DIARY_REMINDER_TIME))
                .thenReturn(SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        when(prefs.getString(SettingsActivity.KEY_WAKING_HOURS_START,
                SettingsActivity.DEFAULT_WAKING_HOURS_START))
                .thenReturn(SettingsActivity.DEFAULT_WAKING_HOURS_START);
        when(prefs.getString(SettingsActivity.KEY_WAKING_HOURS_END,
                SettingsActivity.DEFAULT_WAKING_HOURS_END))
                .thenReturn(SettingsActivity.DEFAULT_WAKING_HOURS_END);

        // AlarmManager is null
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(null);

        ReminderScheduler.rescheduleAll(context);
        // Should not throw when AlarmManager is null
    }

    @Test
    void testRescheduleAllWithMockedAlarmManager() {
        AlarmManager alarmManager = mock(AlarmManager.class);
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager);

        when(prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3);
        when(prefs.getString(SettingsActivity.KEY_SLEEP_REMINDER_TIME,
                SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME))
                .thenReturn("22:00");
        when(prefs.getString(SettingsActivity.KEY_DIARY_REMINDER_TIME,
                SettingsActivity.DEFAULT_DIARY_REMINDER_TIME))
                .thenReturn("20:00");

        ReminderScheduler.rescheduleAll(context);

        // Verify alarm was scheduled for both sleep and diary
        verify(alarmManager, times(2)).setInexactRepeating(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(AlarmManager.INTERVAL_DAY), any());
    }

    @Test
    void testRescheduleAllCancelsDisabledReminders() {
        AlarmManager alarmManager = mock(AlarmManager.class);
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager);

        when(prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3);

        ReminderScheduler.rescheduleAll(context);

        // When disabled, sleep + diary are cancelled (1 each) + 10 emotion slots = 12 cancels
        // PendingIntent.getBroadcast returns null with returnDefaultValues, so cancel is called with null
        verify(alarmManager, times(12)).cancel((android.app.PendingIntent) isNull());
    }

    @Test
    void testRescheduleAllSchedulesEmotionReminders() {
        AlarmManager alarmManager = mock(AlarmManager.class);
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager);

        when(prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(3);
        when(prefs.getString(SettingsActivity.KEY_WAKING_HOURS_START,
                SettingsActivity.DEFAULT_WAKING_HOURS_START))
                .thenReturn("07:00");
        when(prefs.getString(SettingsActivity.KEY_WAKING_HOURS_END,
                SettingsActivity.DEFAULT_WAKING_HOURS_END))
                .thenReturn("22:00");

        ReminderScheduler.rescheduleAll(context);

        // 10 emotion slots cancelled + 3 emotion reminders scheduled
        verify(alarmManager, times(3)).setInexactRepeating(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(AlarmManager.INTERVAL_DAY), any());
    }

    @Test
    void testRescheduleAllWithSingleEmotionReminder() {
        AlarmManager alarmManager = mock(AlarmManager.class);
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager);

        when(prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false)).thenReturn(false);
        when(prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false)).thenReturn(true);
        when(prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT)).thenReturn(1);
        when(prefs.getString(SettingsActivity.KEY_WAKING_HOURS_START,
                SettingsActivity.DEFAULT_WAKING_HOURS_START))
                .thenReturn("08:00");
        when(prefs.getString(SettingsActivity.KEY_WAKING_HOURS_END,
                SettingsActivity.DEFAULT_WAKING_HOURS_END))
                .thenReturn("20:00");

        ReminderScheduler.rescheduleAll(context);

        // 1 emotion reminder scheduled
        verify(alarmManager, times(1)).setInexactRepeating(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(AlarmManager.INTERVAL_DAY), any());
    }
}
