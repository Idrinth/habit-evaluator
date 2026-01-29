package de.idrinth.habitevaluator.shared.model;

import java.util.Objects;

/**
 * Represents the score for a single habit within a week.
 */
public class HabitScore {

    private String habitId;
    private String habitName;
    private String categoryId;
    private int completionCount;
    private int score;

    public HabitScore() {
    }

    public HabitScore(String habitId, String habitName, int completionCount, int score) {
        this.habitId = habitId;
        this.habitName = habitName;
        this.completionCount = completionCount;
        this.score = score;
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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public void setCompletionCount(int completionCount) {
        this.completionCount = completionCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitScore that = (HabitScore) o;
        return Objects.equals(habitId, that.habitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(habitId);
    }
}
