package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating diary statistics: weekly averages and monthly trends.
 */
public class DiaryService {

    /**
     * Calculates the total points for entries on a given date.
     */
    public int getDayPoints(List<DiaryEntry> entries, LocalDate date) {
        return entries.stream()
                .filter(e -> e.getEventDate().equals(date))
                .mapToInt(DiaryEntry::getPoints)
                .sum();
    }

    /**
     * Calculates the total points for the current week (Monday to Sunday).
     */
    public int getCurrentWeekPoints(List<DiaryEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        return getPointsInRange(entries, weekStart, weekEnd);
    }

    /**
     * Calculates the average points per week for the current month.
     * Divides total month points by the number of weeks elapsed (at least 1).
     */
    public double getWeeklyAverageForMonth(List<DiaryEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int totalPoints = getPointsInRange(entries, monthStart, today);
        long daysElapsed = ChronoUnit.DAYS.between(monthStart, today) + 1;
        double weeksElapsed = Math.max(1.0, daysElapsed / 7.0);
        return totalPoints / weeksElapsed;
    }

    /**
     * Calculates the monthly trend by comparing the current week's points
     * to the average of previous weeks in the month.
     *
     * @return positive value means improving, negative means declining, 0 means stable or not enough data
     */
    public double getMonthlyTrend(List<DiaryEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // If the current week start is before the month start, use month start
        LocalDate effectiveCurrentWeekStart = currentWeekStart.isBefore(monthStart)
                ? monthStart : currentWeekStart;

        int currentWeekPoints = getPointsInRange(entries, effectiveCurrentWeekStart, today);

        // Calculate previous weeks' average within this month
        LocalDate previousEnd = effectiveCurrentWeekStart.minusDays(1);
        if (previousEnd.isBefore(monthStart)) {
            // No previous weeks in this month to compare
            return 0.0;
        }

        int previousPoints = getPointsInRange(entries, monthStart, previousEnd);
        long previousDays = ChronoUnit.DAYS.between(monthStart, previousEnd) + 1;
        double previousWeeks = Math.max(1.0, previousDays / 7.0);
        double previousWeeklyAvg = previousPoints / previousWeeks;

        if (previousWeeklyAvg == 0) {
            return currentWeekPoints > 0 ? 1.0 : 0.0;
        }

        return (currentWeekPoints - previousWeeklyAvg) / previousWeeklyAvg;
    }

    /**
     * Gets entries for a specific date range.
     */
    public List<DiaryEntry> getEntriesInRange(List<DiaryEntry> entries, LocalDate start, LocalDate end) {
        return entries.stream()
                .filter(e -> !e.getEventDate().isBefore(start) && !e.getEventDate().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * Calculates total points within a date range (inclusive).
     */
    public int getPointsInRange(List<DiaryEntry> entries, LocalDate start, LocalDate end) {
        return entries.stream()
                .filter(e -> !e.getEventDate().isBefore(start) && !e.getEventDate().isAfter(end))
                .mapToInt(DiaryEntry::getPoints)
                .sum();
    }

    /**
     * Gets the total points for the current month.
     */
    public int getCurrentMonthPoints(List<DiaryEntry> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return getPointsInRange(entries, monthStart, today);
    }
}
