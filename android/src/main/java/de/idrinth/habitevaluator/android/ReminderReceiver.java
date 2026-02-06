package de.idrinth.habitevaluator.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

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
                showNotification(context, NOTIFICATION_SLEEP,
                        context.getString(R.string.reminder_sleep_notification_title),
                        context.getString(R.string.reminder_sleep_notification_text));
                break;
            case TYPE_DIARY:
                showNotification(context, NOTIFICATION_DIARY,
                        context.getString(R.string.reminder_diary_notification_title),
                        context.getString(R.string.reminder_diary_notification_text));
                break;
            case TYPE_EMOTION:
                showNotification(context, NOTIFICATION_EMOTION,
                        context.getString(R.string.reminder_emotion_notification_title),
                        context.getString(R.string.reminder_emotion_notification_text));
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
