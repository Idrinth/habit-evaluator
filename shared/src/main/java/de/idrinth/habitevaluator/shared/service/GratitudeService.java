package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating gratitude diary statistics.
 */
public class GratitudeService {

    /**
     * Counts entries for a given date.
     */
    public int getDayCount(List<GratitudeEntry> entries, LocalDate date) {
        return (int) entries.stream()
                .filter(e -> e.getEventDate().equals(date))
                .count();
    }

    /**
     * Counts entries for the current week (Monday to Sunday).
     */
    public int getCurrentWeekCount(List<GratitudeEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        return getCountInRange(entries, weekStart, weekEnd);
    }

    /**
     * Counts entries for the current month.
     */
    public int getCurrentMonthCount(List<GratitudeEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return getCountInRange(entries, monthStart, today);
    }

    /**
     * Calculates the average entries per day for the current month.
     */
    public double getDailyAverageForMonth(List<GratitudeEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int totalCount = getCountInRange(entries, monthStart, today);
        long daysElapsed = ChronoUnit.DAYS.between(monthStart, today) + 1;
        return totalCount / (double) Math.max(1, daysElapsed);
    }

    /**
     * Gets entries within a date range (inclusive).
     */
    public List<GratitudeEntry> getEntriesInRange(List<GratitudeEntry> entries, LocalDate start, LocalDate end) {
        return entries.stream()
                .filter(e -> !e.getEventDate().isBefore(start) && !e.getEventDate().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * Counts entries within a date range (inclusive).
     */
    public int getCountInRange(List<GratitudeEntry> entries, LocalDate start, LocalDate end) {
        return (int) entries.stream()
                .filter(e -> !e.getEventDate().isBefore(start) && !e.getEventDate().isAfter(end))
                .count();
    }

    /**
     * Calculates the current streak of consecutive days with at least one gratitude entry.
     */
    public int getCurrentStreak(List<GratitudeEntry> entries) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate date = today;
        while (getDayCount(entries, date) > 0) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }
}
