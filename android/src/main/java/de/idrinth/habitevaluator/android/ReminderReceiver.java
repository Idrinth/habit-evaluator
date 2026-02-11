package de.idrinth.habitevaluator.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import de.idrinth.habitevaluator.android.persistence.SQLiteHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Receives scheduled alarm broadcasts and shows reminder notifications.
 * Also reschedules reminders after device boot.
 */
public class ReminderReceiver extends BroadcastReceiver {

    static final String ACTION_REMINDER = "de.idrinth.habitevaluator.REMINDER";
    static final String EXTRA_TYPE = "reminder_type";
    static final String TYPE_SLEEP = "sleep";
    static final String TYPE_DIARY = "diary";
    static final String TYPE_EMOTION = "emotion";

    private static final String CHANNEL_ID = "habit_evaluator_reminders";
    private static final int NOTIFICATION_SLEEP = 1001;
    private static final int NOTIFICATION_DIARY = 1002;
    private static final int NOTIFICATION_EMOTION = 1003;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ReminderScheduler.rescheduleAll(context);
            return;
        }

        if (!ACTION_REMINDER.equals(intent.getAction())) {
            return;
        }

        String type = intent.getStringExtra(EXTRA_TYPE);
        if (type == null) {
            return;
        }

        ensureNotificationChannel(context);

        switch (type) {
            case TYPE_SLEEP:
                if (!hasSleepEntryForToday(context)) {
                    showNotification(context, NOTIFICATION_SLEEP,
                            context.getString(R.string.reminder_sleep_notification_title),
                            context.getString(R.string.reminder_sleep_notification_text));
                }
                break;
            case TYPE_DIARY:
                if (!hasDiaryEntryForToday(context)) {
                    showNotification(context, NOTIFICATION_DIARY,
                            context.getString(R.string.reminder_diary_notification_title),
                            context.getString(R.string.reminder_diary_notification_text));
                }
                break;
            case TYPE_EMOTION:
                if (!hasEnoughEmotionEntriesForToday(context)) {
                    showNotification(context, NOTIFICATION_EMOTION,
                            context.getString(R.string.reminder_emotion_notification_title),
                            context.getString(R.string.reminder_emotion_notification_text));
                }
                break;
        }
    }

    private void ensureNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.reminder_channel_description));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private boolean hasSleepEntryForToday(Context context) {
        try {
            SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            try (Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM sleep_entries WHERE date = ? LIMIT 1",
                    new String[]{today})) {
                return cursor.moveToFirst();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasDiaryEntryForToday(Context context) {
        try {
            SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            try (Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM diary_entries WHERE event_date = ? LIMIT 1",
                    new String[]{today})) {
                return cursor.moveToFirst();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasEnoughEmotionEntriesForToday(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(
                    SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
            int requiredCount = prefs.getInt(SettingsActivity.KEY_EMOTION_REMINDER_COUNT,
                    SettingsActivity.DEFAULT_EMOTION_REMINDER_COUNT);
            SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            try (Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM emotion_entries WHERE recorded_at LIKE ?",
                    new String[]{today + "%"})) {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0) >= requiredCount;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void showNotification(Context context, int notificationId,
                                   String title, String text) {
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }
}
