package de.idrinth.habitevaluator.desktop;

import de.idrinth.habitevaluator.shared.api.StorageConfig;
import de.idrinth.habitevaluator.shared.util.ReminderScheduleCalculator;
import javafx.application.Platform;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Schedules and fires desktop reminders using system tray notifications.
 * All reminders are disabled by default and must be explicitly enabled in settings.
 */
public class ReminderService {

    private ScheduledExecutorService scheduler;
    private final StorageConfig config;

    public ReminderService(StorageConfig config) {
        this.config = config;
    }

    /**
     * Cancels any existing reminders and reschedules based on current config.
     */
    public void reschedule() {
        stop();
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "reminder-scheduler");
            t.setDaemon(true);
            return t;
        });

        if (config.isSleepReminderEnabled()) {
            scheduleDailyAt(config.getSleepReminderTime(),
                    "Log your sleep",
                    "Don't forget to record last night's sleep data.");
        }

        if (config.isDiaryReminderEnabled()) {
            scheduleDailyAt(config.getDiaryReminderTime(),
                    "Positivity diary",
                    "You haven't added a diary entry today. Any positive moments to record?");
        }

        if (config.isEmotionReminderEnabled()) {
            scheduleRandomEmotionReminders();
        }
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void scheduleDailyAt(String timeStr, String title, String message) {
        long delaySeconds = secondsUntil(timeStr);
        scheduler.scheduleAtFixedRate(
                () -> showNotification(title, message),
                delaySeconds,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
    }

    private void scheduleRandomEmotionReminders() {
        int count = config.getEmotionReminderCount();
        String startStr = config.getWakingHoursStart();
        String endStr = config.getWakingHoursEnd();

        List<String> times = ReminderScheduleCalculator.calculateEmotionReminderTimes(startStr, endStr, count);
        for (String time : times) {
            long delaySeconds = secondsUntil(time);
            scheduler.scheduleAtFixedRate(
                    () -> showNotification("How are you feeling?",
                            "Take a moment to record your current emotional state."),
                    delaySeconds,
                    TimeUnit.DAYS.toSeconds(1),
                    TimeUnit.SECONDS
            );
        }
    }

    private long secondsUntil(String timeStr) {
        int[] hm = ReminderScheduleCalculator.parseTime(timeStr);
        LocalTime target = LocalTime.of(hm[0], hm[1]);
        LocalTime now = LocalTime.now();
        long seconds = now.until(target, ChronoUnit.SECONDS);
        if (seconds <= 0) {
            seconds += TimeUnit.DAYS.toSeconds(1);
        }
        return seconds;
    }

    private void showNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                java.awt.Image image = Toolkit.getDefaultToolkit()
                        .createImage(getClass().getResource("/logo.svg") != null
                                ? getClass().getResource("/logo.svg")
                                : getClass().getResource("/icon.png"));
                TrayIcon trayIcon = new TrayIcon(
                        image != null ? image : Toolkit.getDefaultToolkit().createImage(new byte[0]),
                        "Habit Evaluator");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                // Remove icon after a delay to avoid cluttering the tray
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    tray.remove(trayIcon);
                }).start();
            } catch (Exception e) {
                // System tray not available or notification failed - silent fallback
            }
        }
    }

}
