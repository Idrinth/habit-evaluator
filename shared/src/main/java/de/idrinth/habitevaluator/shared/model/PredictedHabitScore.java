package de.idrinth.habitevaluator.shared.model;

/**
 * Represents the predicted end-of-week score for a single habit,
 * based on the current completion rate extrapolated over remaining days.
 */
public class PredictedHabitScore {

    private String habitId;
    private String habitName;
    private String categoryId;
    private int currentCompletionCount;
    private int currentScore;
    private int predictedCompletionCount;
    private int predictedScore;
    private double dailyRate;

    public PredictedHabitScore() {
    }

    public PredictedHabitScore(String habitId, String habitName, int currentCompletionCount,
                                int currentScore, int predictedCompletionCount, int predictedScore,
                                double dailyRate) {
        this.habitId = habitId;
        this.habitName = habitName;
        this.currentCompletionCount = currentCompletionCount;
        this.currentScore = currentScore;
        this.predictedCompletionCount = predictedCompletionCount;
        this.predictedScore = predictedScore;
        this.dailyRate = dailyRate;
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

    public int getCurrentCompletionCount() {
        return currentCompletionCount;
    }

    public void setCurrentCompletionCount(int currentCompletionCount) {
        this.currentCompletionCount = currentCompletionCount;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getPredictedCompletionCount() {
        return predictedCompletionCount;
    }

    public void setPredictedCompletionCount(int predictedCompletionCount) {
        this.predictedCompletionCount = predictedCompletionCount;
    }

    public int getPredictedScore() {
        return predictedScore;
    }

    public void setPredictedScore(int predictedScore) {
        this.predictedScore = predictedScore;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }
}
