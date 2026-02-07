package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDate;

/**
 * Holds aggregated sport log statistics for a period:
 * average/min/max duration hours, average/min/max measurement, and total entries.
 */
public class SportLogStats {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private double averageDurationHours;
    private double minDurationHours;
    private double maxDurationHours;
    private double averageMeasurement;
    private double minMeasurement;
    private double maxMeasurement;
    private int totalEntries;

    public SportLogStats() {
    }

    public SportLogStats(LocalDate periodStart, LocalDate periodEnd,
                         double averageDurationHours, double minDurationHours, double maxDurationHours,
                         double averageMeasurement, double minMeasurement, double maxMeasurement,
                         int totalEntries) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.averageDurationHours = averageDurationHours;
        this.minDurationHours = minDurationHours;
        this.maxDurationHours = maxDurationHours;
        this.averageMeasurement = averageMeasurement;
        this.minMeasurement = minMeasurement;
        this.maxMeasurement = maxMeasurement;
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

    public double getAverageDurationHours() {
        return averageDurationHours;
    }

    public void setAverageDurationHours(double averageDurationHours) {
        this.averageDurationHours = averageDurationHours;
    }

    public double getMinDurationHours() {
        return minDurationHours;
    }

    public void setMinDurationHours(double minDurationHours) {
        this.minDurationHours = minDurationHours;
    }

    public double getMaxDurationHours() {
        return maxDurationHours;
    }

    public void setMaxDurationHours(double maxDurationHours) {
        this.maxDurationHours = maxDurationHours;
    }

    public double getAverageMeasurement() {
        return averageMeasurement;
    }

    public void setAverageMeasurement(double averageMeasurement) {
        this.averageMeasurement = averageMeasurement;
    }

    public double getMinMeasurement() {
        return minMeasurement;
    }

    public void setMinMeasurement(double minMeasurement) {
        this.minMeasurement = minMeasurement;
    }

    public double getMaxMeasurement() {
        return maxMeasurement;
    }

    public void setMaxMeasurement(double maxMeasurement) {
        this.maxMeasurement = maxMeasurement;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }
}
