package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a predicted end-of-week score based on current week data
 * and completion rate extrapolation.
 */
public class PredictedWeeklyScore {

    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int weekNumber;
    private int year;
    private LocalDateTime calculatedAt;
    private int daysElapsed;
    private int daysRemaining;
    private int currentTotalScore;
    private int predictedTotalScore;
    private List<PredictedHabitScore> habitPredictions;

    public PredictedWeeklyScore() {
        this.habitPredictions = new ArrayList<>();
        this.calculatedAt = LocalDateTime.now();
    }

    public PredictedWeeklyScore(LocalDate weekStart, LocalDate weekEnd, int daysElapsed, int daysRemaining) {
        this();
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.daysElapsed = daysElapsed;
        this.daysRemaining = daysRemaining;
        this.weekNumber = weekStart.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        this.year = weekStart.get(java.time.temporal.WeekFields.ISO.weekBasedYear());
    }

    public void addHabitPrediction(PredictedHabitScore prediction) {
        this.habitPredictions.add(prediction);
        recalculateScores();
    }

    public void recalculateScores() {
        this.currentTotalScore = habitPredictions.stream()
                .mapToInt(PredictedHabitScore::getCurrentScore)
                .sum();
        this.predictedTotalScore = habitPredictions.stream()
                .mapToInt(PredictedHabitScore::getPredictedScore)
                .sum();
    }

    public int getPredictedScoreByCategory(String categoryId) {
        return habitPredictions.stream()
                .filter(hp -> Objects.equals(hp.getCategoryId(), categoryId))
                .mapToInt(PredictedHabitScore::getPredictedScore)
                .sum();
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    public void setDaysElapsed(int daysElapsed) {
        this.daysElapsed = daysElapsed;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public int getCurrentTotalScore() {
        return currentTotalScore;
    }

    public void setCurrentTotalScore(int currentTotalScore) {
        this.currentTotalScore = currentTotalScore;
    }

    public int getPredictedTotalScore() {
        return predictedTotalScore;
    }

    public void setPredictedTotalScore(int predictedTotalScore) {
        this.predictedTotalScore = predictedTotalScore;
    }

    public List<PredictedHabitScore> getHabitPredictions() {
        return habitPredictions;
    }

    public void setHabitPredictions(List<PredictedHabitScore> habitPredictions) {
        this.habitPredictions = habitPredictions;
        recalculateScores();
    }
}
