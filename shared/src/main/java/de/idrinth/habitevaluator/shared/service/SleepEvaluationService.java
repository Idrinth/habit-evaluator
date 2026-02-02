package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SleepStats;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
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

        double totalHours = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (SleepEntry entry : periodEntries) {
            totalHours += entry.getHours();
            if (entry.getHours() < min) {
                min = entry.getHours();
            }
            if (entry.getHours() > max) {
                max = entry.getHours();
            }
        }

        double average = totalHours / periodEntries.size();

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
}
