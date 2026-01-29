package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.HabitScore;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.WeeklyScore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Service for calculating habit scores and weekly totals.
 */
public class HabitScoringService {

    /**
     * Calculates the score for a habit based on completions in a given week.
     *
     * @param habit     the habit to score
     * @param weekStart start of the week (Monday)
     * @param weekEnd   end of the week (Sunday)
     * @return the calculated HabitScore
     */
    public HabitScore calculateHabitScore(Habit habit, LocalDate weekStart, LocalDate weekEnd) {
        int completionCount = countCompletionsInPeriod(habit, weekStart, weekEnd);

        ScoringRule rule = habit.getScoringRule();
        if (rule == null) {
            rule = new ScoringRule();
        }

        int score = rule.calculateScore(completionCount);

        HabitScore habitScore = new HabitScore(habit.getId(), habit.getName(), completionCount, score);
        habitScore.setCategoryId(habit.getCategoryId());

        return habitScore;
    }

    /**
     * Calculates the weekly score for all provided habits.
     *
     * @param habits    list of habits to score
     * @param weekStart start of the week (Monday)
     * @param weekEnd   end of the week (Sunday)
     * @return WeeklyScore containing all habit scores and total
     */
    public WeeklyScore calculateWeeklyScore(List<Habit> habits, LocalDate weekStart, LocalDate weekEnd) {
        WeeklyScore weeklyScore = new WeeklyScore(weekStart, weekEnd);

        for (Habit habit : habits) {
            HabitScore habitScore = calculateHabitScore(habit, weekStart, weekEnd);
            weeklyScore.addHabitScore(habitScore);
        }

        return weeklyScore;
    }

    /**
     * Calculates the weekly score for the current week.
     *
     * @param habits list of habits to score
     * @return WeeklyScore for the current week
     */
    public WeeklyScore calculateCurrentWeekScore(List<Habit> habits) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return calculateWeeklyScore(habits, weekStart, weekEnd);
    }

    /**
     * Calculates the weekly score for a specific week number and year.
     *
     * @param habits     list of habits to score
     * @param weekNumber the ISO week number
     * @param year       the year
     * @return WeeklyScore for the specified week
     */
    public WeeklyScore calculateWeeklyScoreByWeekNumber(List<Habit> habits, int weekNumber, int year) {
        LocalDate weekStart = LocalDate.of(year, 1, 4)
                .with(java.time.temporal.WeekFields.ISO.weekBasedYear(), year)
                .with(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear(), weekNumber)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        return calculateWeeklyScore(habits, weekStart, weekEnd);
    }

    /**
     * Gets the score for a single habit in the current week.
     *
     * @param habit the habit to score
     * @return the score (0, 1, 2, 4, or 8)
     */
    public int getCurrentWeekScore(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return calculateHabitScore(habit, weekStart, weekEnd).getScore();
    }

    /**
     * Counts the number of completions for a habit within a date range.
     *
     * @param habit the habit
     * @param start start date (inclusive)
     * @param end   end date (inclusive)
     * @return number of completions
     */
    public int countCompletionsInPeriod(Habit habit, LocalDate start, LocalDate end) {
        return (int) habit.getEntries().stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getCompletedAt().toLocalDate();
                    return !entryDate.isBefore(start) && !entryDate.isAfter(end);
                })
                .count();
    }
}
