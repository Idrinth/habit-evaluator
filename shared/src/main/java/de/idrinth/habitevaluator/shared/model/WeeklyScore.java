package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the total scores for all habits in a given week.
 */
public class WeeklyScore {

    private String id;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int weekNumber;
    private int year;
    private List<HabitScore> habitScores;
    private int totalScore;

    public WeeklyScore() {
        this.id = UUID.randomUUID().toString();
        this.habitScores = new ArrayList<>();
    }

    public WeeklyScore(LocalDate weekStart, LocalDate weekEnd) {
        this();
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.weekNumber = weekStart.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        this.year = weekStart.get(java.time.temporal.WeekFields.ISO.weekBasedYear());
    }

    public void addHabitScore(HabitScore habitScore) {
        this.habitScores.add(habitScore);
        recalculateTotalScore();
    }

    public void recalculateTotalScore() {
        this.totalScore = habitScores.stream()
                .mapToInt(HabitScore::getScore)
                .sum();
    }

    /**
     * Gets total score for a specific category.
     *
     * @param categoryId the category ID
     * @return sum of scores for habits in that category
     */
    public int getScoreByCategory(String categoryId) {
        return habitScores.stream()
                .filter(hs -> Objects.equals(hs.getCategoryId(), categoryId))
                .mapToInt(HabitScore::getScore)
                .sum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<HabitScore> getHabitScores() {
        return habitScores;
    }

    public void setHabitScores(List<HabitScore> habitScores) {
        this.habitScores = habitScores;
        recalculateTotalScore();
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyScore that = (WeeklyScore) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
