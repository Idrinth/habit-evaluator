package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDate;

/**
 * Holds aggregated sleep statistics (average, min, max hours) for a period.
 */
public class SleepStats {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private double averageHours;
    private double minHours;
    private double maxHours;
    private int totalEntries;

    public SleepStats() {
    }

    public SleepStats(LocalDate periodStart, LocalDate periodEnd, double averageHours,
                      double minHours, double maxHours, int totalEntries) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.averageHours = averageHours;
        this.minHours = minHours;
        this.maxHours = maxHours;
        this.totalEntries = totalEntries;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public double getAverageHours() {
        return averageHours;
    }

    public void setAverageHours(double averageHours) {
        this.averageHours = averageHours;
    }

    public double getMinHours() {
        return minHours;
    }

    public void setMinHours(double minHours) {
        this.minHours = minHours;
    }

    public double getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(double maxHours) {
        this.maxHours = maxHours;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }
}
