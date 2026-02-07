package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.SportLogStats;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating sport log statistics: average, min, and max duration
 * and measurement values per period.
 */
public class SportLogService {

    /**
     * Calculates sport log statistics for a given period.
     *
     * @param entries     all sport log entries to consider
     * @param periodStart start of the period (inclusive)
     * @param periodEnd   end of the period (inclusive)
     * @return aggregated sport log stats for the period
     */
    public SportLogStats calculateStats(List<SportLog> entries, LocalDate periodStart, LocalDate periodEnd) {
        List<SportLog> periodEntries = entries.stream()
                .filter(entry -> !entry.getDate().isBefore(periodStart) && !entry.getDate().isAfter(periodEnd))
                .collect(Collectors.toList());

        if (periodEntries.isEmpty()) {
            return new SportLogStats(periodStart, periodEnd, 0, 0, 0, 0, 0, 0, 0);
        }

        double totalDuration = 0;
        double minDuration = Double.MAX_VALUE;
        double maxDuration = Double.MIN_VALUE;
        double totalMeasurement = 0;
        double minMeasurement = Double.MAX_VALUE;
        double maxMeasurement = Double.MIN_VALUE;

        for (SportLog entry : periodEntries) {
            double duration = entry.getDurationHours();
            totalDuration += duration;
            if (duration < minDuration) {
                minDuration = duration;
            }
            if (duration > maxDuration) {
                maxDuration = duration;
            }

            double m = entry.getMeasurement();
            totalMeasurement += m;
            if (m < minMeasurement) {
                minMeasurement = m;
            }
            if (m > maxMeasurement) {
                maxMeasurement = m;
            }
        }

        double avgDuration = totalDuration / periodEntries.size();
        double avgMeasurement = totalMeasurement / periodEntries.size();

        return new SportLogStats(periodStart, periodEnd,
                avgDuration, minDuration, maxDuration,
                avgMeasurement, minMeasurement, maxMeasurement,
                periodEntries.size());
    }

    /**
     * Calculates sport log statistics for the current week (Monday to today).
     */
    public SportLogStats getCurrentWeekStats(List<SportLog> entries) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return calculateStats(entries, weekStart, today);
    }

    /**
     * Calculates sport log statistics for the current month (1st to today).
     */
    public SportLogStats getCurrentMonthStats(List<SportLog> entries) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return calculateStats(entries, monthStart, today);
    }
}
