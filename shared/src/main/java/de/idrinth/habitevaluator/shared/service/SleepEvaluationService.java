package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SleepStats;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating sleep statistics: average, min, and max hours per period.
 */
public class SleepEvaluationService {

    /**
     * Calculates sleep statistics for a given period.
     *
     * @param entries     all sleep entries to consider
     * @param periodStart start of the period (inclusive)
     * @param periodEnd   end of the period (inclusive)
     * @return aggregated sleep stats for the period
     */
    public SleepStats calculateStats(List<SleepEntry> entries, LocalDate periodStart, LocalDate periodEnd) {
        List<SleepEntry> periodEntries = entries.stream()
                .filter(entry -> !entry.getDate().isBefore(periodStart) && !entry.getDate().isAfter(periodEnd))
                .collect(Collectors.toList());

        if (periodEntries.isEmpty()) {
            return new SleepStats(periodStart, periodEnd, 0, 0, 0, 0);
        }

        Map<LocalDate, Double> hoursPerDay = new HashMap<>();
        for (SleepEntry entry : periodEntries) {
            hoursPerDay.merge(entry.getDate(), entry.getHours(), Double::sum);
        }

        double totalHours = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double dayHours : hoursPerDay.values()) {
            totalHours += dayHours;
            if (dayHours < min) {
                min = dayHours;
            }
            if (dayHours > max) {
                max = dayHours;
            }
        }

        double average = totalHours / hoursPerDay.size();

        return new SleepStats(periodStart, periodEnd, average, min, max, periodEntries.size());
    }

    /**
     * Calculates sleep statistics for the current week (Monday to today).
     */
    public SleepStats getCurrentWeekStats(List<SleepEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return calculateStats(entries, weekStart, today);
    }

    /**
     * Calculates sleep statistics for the current month (1st to today).
     */
    public SleepStats getCurrentMonthStats(List<SleepEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return calculateStats(entries, monthStart, today);
    }

    /**
     * Checks if a new sleep entry overlaps with any existing entry, accounting for
     * entries that cross the midnight boundary onto the next day.
     *
     * @param entries  all existing sleep entries
     * @param date     date of the new entry
     * @param newFrom  start time of the new entry
     * @param newUntil end time of the new entry
     * @return true if the new entry overlaps with an existing one
     */
    public boolean hasOverlap(List<SleepEntry> entries, LocalDate date, LocalTime newFrom, LocalTime newUntil) {
        if (entries == null) {
            return false;
        }

        long newStartMinutes = toAbsoluteMinutes(date, newFrom, false);
        long newEndMinutes = toAbsoluteMinutes(date, newUntil, crossesMidnight(newFrom, newUntil));

        for (SleepEntry existing : entries) {
            long existingStart = toAbsoluteMinutes(existing.getDate(), existing.getFromTime(), false);
            long existingEnd = toAbsoluteMinutes(
                    existing.getDate(),
                    existing.getUntilTime(),
                    crossesMidnight(existing.getFromTime(), existing.getUntilTime())
            );

            if (newStartMinutes < existingEnd && newEndMinutes > existingStart) {
                return true;
            }
        }
        return false;
    }

    private boolean crossesMidnight(LocalTime from, LocalTime until) {
        int fromMinutes = from.getHour() * 60 + from.getMinute();
        int untilMinutes = until.getHour() * 60 + until.getMinute();
        return untilMinutes <= fromMinutes;
    }

    private long toAbsoluteMinutes(LocalDate date, LocalTime time, boolean nextDay) {
        long dayOffset = date.toEpochDay() * 24 * 60;
        long timeMinutes = time.getHour() * 60 + time.getMinute();
        if (nextDay) {
            dayOffset += 24 * 60;
        }
        return dayOffset + timeMinutes;
    }
}
