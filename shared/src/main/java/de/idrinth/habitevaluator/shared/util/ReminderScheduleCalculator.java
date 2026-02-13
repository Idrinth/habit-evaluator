package de.idrinth.habitevaluator.shared.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Platform-agnostic utility for calculating reminder schedule times.
 * Used by both Android (ReminderScheduler) and Desktop (ReminderService)
 * to compute emotion reminder times within waking hours.
 */
public final class ReminderScheduleCalculator {

    private ReminderScheduleCalculator() {
        // Utility class
    }

    /**
     * Parses a time string in "HH:mm" format into an int array of [hour, minute].
     * Returns [8, 0] as default if parsing fails.
     */
    public static int[] parseTime(String time) {
        try {
            String[] parts = time.split(":");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            return new int[]{8, 0};
        }
    }

    /**
     * Calculates random emotion reminder times distributed within the waking hours window.
     *
     * @param wakingHoursStart start of waking hours in "HH:mm" format
     * @param wakingHoursEnd   end of waking hours in "HH:mm" format
     * @param count            number of reminders to schedule (capped at 10)
     * @return list of time strings in "HH:mm" format
     */
    public static List<String> calculateEmotionReminderTimes(String wakingHoursStart, String wakingHoursEnd, int count) {
        int[] start = parseTime(wakingHoursStart);
        int[] end = parseTime(wakingHoursEnd);
        int startMinutes = start[0] * 60 + start[1];
        int endMinutes = end[0] * 60 + end[1];
        if (endMinutes <= startMinutes) {
            endMinutes = startMinutes + 60;
        }
        int range = endMinutes - startMinutes;

        List<String> times = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < Math.min(count, 10); i++) {
            int offsetMinutes = random.nextInt(range);
            int totalMinutes = startMinutes + offsetMinutes;
            int hour = totalMinutes / 60;
            int minute = totalMinutes % 60;
            times.add(String.format("%02d:%02d", hour, minute));
        }
        return times;
    }
}
