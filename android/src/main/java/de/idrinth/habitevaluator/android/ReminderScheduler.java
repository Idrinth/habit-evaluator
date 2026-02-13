package de.idrinth.habitevaluator.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import de.idrinth.habitevaluator.shared.util.ReminderScheduleCalculator;

import java.util.Calendar;
import java.util.List;

/**
 * Schedules and cancels reminder alarms using AlarmManager.
 * All reminders are disabled by default.
 */
public final class ReminderScheduler {

    private static final int SLEEP_REMINDER_REQUEST_CODE = 9001;
    private static final int DIARY_REMINDER_REQUEST_CODE = 9002;
    private static final int EMOTION_REMINDER_BASE_REQUEST_CODE = 9100;

    private ReminderScheduler() {
    }

    /**
     * Re-schedules all enabled reminders based on current SharedPreferences.
     * Cancels any that are disabled.
     */
    public static void rescheduleAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);

        scheduleSleepReminder(context, prefs);
        scheduleDiaryReminder(context, prefs);
        scheduleEmotionReminders(context, prefs);
    }

    private static void scheduleSleepReminder(Context context, SharedPreferences prefs) {
        boolean enabled = prefs.getBoolean(SettingsActivity.KEY_SLEEP_REMINDER_ENABLED, false);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createPendingIntent(context, SLEEP_REMINDER_REQUEST_CODE,
                ReminderReceiver.TYPE_SLEEP);

        if (!enabled || alarmManager == null) {
            if (alarmManager != null) {
                alarmManager.cancel(pi);
            }
            return;
        }

        String time = prefs.getString(SettingsActivity.KEY_SLEEP_REMINDER_TIME,
                SettingsActivity.DEFAULT_SLEEP_REMINDER_TIME);
        int[] hm = ReminderScheduleCalculator.parseTime(time);
        scheduleDaily(alarmManager, pi, hm[0], hm[1]);
    }

    private static void scheduleDiaryReminder(Context context, SharedPreferences prefs) {
        boolean enabled = prefs.getBoolean(SettingsActivity.KEY_DIARY_REMINDER_ENABLED, false);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createPendingIntent(context, DIARY_REMINDER_REQUEST_CODE,
                ReminderReceiver.TYPE_DIARY);

        if (!enabled || alarmManager == null) {
            if (alarmManager != null) {
                alarmManager.cancel(pi);
            }
            return;
        }

        String time = prefs.getString(SettingsActivity.KEY_DIARY_REMINDER_TIME,
                SettingsActivity.DEFAULT_DIARY_REMINDER_TIME);
        int[] hm = ReminderScheduleCalculator.parseTime(time);
        scheduleDaily(alarmManager, pi, hm[0], hm[1]);
    }

    private static void scheduleEmotionReminders(Context context, SharedPreferences prefs) {
        boolean enabled = prefs.getBoolean(SettingsActivity.KEY_EMOTION_REMINDER_ENABLED, false);
        int count = prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel all possible emotion reminder slots (up to max 10)
        for (int i = 0; i < 10; i++) {
            PendingIntent pi = createPendingIntent(context,
                    EMOTION_REMINDER_BASE_REQUEST_CODE + i, ReminderReceiver.TYPE_EMOTION);
            if (alarmManager != null) {
                alarmManager.cancel(pi);
            }
        }

        if (!enabled || alarmManager == null) {
            return;
        }

        String startStr = prefs.getString(SettingsActivity.KEY_WAKING_HOURS_START,
                SettingsActivity.DEFAULT_WAKING_HOURS_START);
        String endStr = prefs.getString(SettingsActivity.KEY_WAKING_HOURS_END,
                SettingsActivity.DEFAULT_WAKING_HOURS_END);

        List<String> times = ReminderScheduleCalculator.calculateEmotionReminderTimes(startStr, endStr, count);
        for (int i = 0; i < times.size(); i++) {
            int[] hm = ReminderScheduleCalculator.parseTime(times.get(i));
            PendingIntent pi = createPendingIntent(context,
                    EMOTION_REMINDER_BASE_REQUEST_CODE + i, ReminderReceiver.TYPE_EMOTION);
            scheduleDaily(alarmManager, pi, hm[0], hm[1]);
        }
    }

    private static void scheduleDaily(AlarmManager alarmManager, PendingIntent pi,
                                       int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pi
        );
    }

    private static PendingIntent createPendingIntent(Context context, int requestCode,
                                                      String type) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_REMINDER);
        intent.putExtra(ReminderReceiver.EXTRA_TYPE, type);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

}
