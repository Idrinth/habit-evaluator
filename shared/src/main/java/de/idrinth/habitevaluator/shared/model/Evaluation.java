package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDate;

/**
 * Represents the evaluation result of a habit over a period.
 */
public class Evaluation {

    private String habitId;
    private String habitName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int totalEntries;
    private int targetEntries;
    private double completionRate;
    private int currentStreak;
    private int longestStreak;

    public Evaluation() {
    }

    public Evaluation(String habitId, String habitName) {
        this.habitId = habitId;
        this.habitName = habitName;
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
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

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public int getTargetEntries() {
        return targetEntries;
    }

    public void setTargetEntries(int targetEntries) {
        this.targetEntries = targetEntries;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public boolean isOnTrack() {
        return completionRate >= 0.8;
    }
}
