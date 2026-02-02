package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for evaluating habit performance and calculating statistics.
 */
public class HabitEvaluatorService {

    /**
     * Evaluates a habit over the specified period.
     *
     * @param habit       the habit to evaluate
     * @param periodStart start of the evaluation period
     * @param periodEnd   end of the evaluation period
     * @return the evaluation result
     */
    public Evaluation evaluate(Habit habit, LocalDate periodStart, LocalDate periodEnd) {
        Evaluation evaluation = new Evaluation(habit.getId(), habit.getName());
        evaluation.setPeriodStart(periodStart);
        evaluation.setPeriodEnd(periodEnd);

        List<HabitEntry> periodEntries = habit.getEntries().stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getCompletedAt().toLocalDate();
                    return !entryDate.isBefore(periodStart) && !entryDate.isAfter(periodEnd);
                })
                .sorted(Comparator.comparing(HabitEntry::getCompletedAt))
                .toList();

        evaluation.setTotalEntries(periodEntries.size());
        evaluation.setTargetEntries(calculateTargetEntries(habit, periodStart, periodEnd));
        evaluation.setPositiveScoring(habit.isPositiveScoring());

        if (habit.isPositiveScoring()) {
            evaluation.setCompletionRate(calculateCompletionRate(evaluation.getTotalEntries(), evaluation.getTargetEntries()));
            evaluation.setCurrentStreak(calculateCurrentStreak(habit, periodEnd));
            evaluation.setLongestStreak(calculateLongestStreak(habit, periodStart, periodEnd));
        } else {
            // For negative habits, rate = avoidance rate (how well they avoided it)
            evaluation.setCompletionRate(calculateAvoidanceRate(evaluation.getTotalEntries(), evaluation.getTargetEntries()));
            // For negative habits, streak = consecutive days WITHOUT completions
            evaluation.setCurrentStreak(calculateCurrentAvoidanceStreak(habit, periodEnd));
            evaluation.setLongestStreak(calculateLongestAvoidanceStreak(habit, periodStart, periodEnd));
        }

        return evaluation;
    }

    /**
     * Evaluates a habit for the current week.
     */
    public Evaluation evaluateCurrentWeek(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return evaluate(habit, weekStart, today);
    }

    /**
     * Evaluates a habit for the current month.
     */
    public Evaluation evaluateCurrentMonth(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return evaluate(habit, monthStart, today);
    }

    private int calculateTargetEntries(Habit habit, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        FrequencyType frequencyType = habit.getFrequencyType();
        int targetFrequency = habit.getTargetFrequency();

        return switch (frequencyType) {
            case DAILY -> (int) days * targetFrequency;
            case WEEKLY -> (int) Math.ceil(days / 7.0) * targetFrequency;
            case MONTHLY -> (int) Math.max(1, ChronoUnit.MONTHS.between(start, end)) * targetFrequency;
        };
    }

    private double calculateCompletionRate(int actual, int target) {
        if (target == 0) {
            return 1.0;
        }
        return Math.min(1.0, (double) actual / target);
    }

    private int calculateCurrentStreak(Habit habit, LocalDate endDate) {
        Set<LocalDate> completedDates = habit.getEntries().stream()
                .map(entry -> entry.getCompletedAt().toLocalDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate checkDate = endDate;

        while (completedDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private int calculateLongestStreak(Habit habit, LocalDate start, LocalDate end) {
        Set<LocalDate> completedDates = habit.getEntries().stream()
                .map(entry -> entry.getCompletedAt().toLocalDate())
                .filter(date -> !date.isBefore(start) && !date.isAfter(end))
                .collect(Collectors.toSet());

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate checkDate = start;

        while (!checkDate.isAfter(end)) {
            if (completedDates.contains(checkDate)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            checkDate = checkDate.plusDays(1);
        }

        return longestStreak;
    }

    /**
     * For negative habits, avoidance rate = 1 - (actual / target), clamped to [0, 1].
     * If target is 0, returns 1.0 (perfect avoidance).
     */
    private double calculateAvoidanceRate(int actual, int target) {
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        return Math.max(0.0, Math.min(1.0, 1.0 - (double) actual / target));
    }

    /**
     * For negative habits, counts consecutive days going backwards from endDate
     * where no completion exists (days the habit was avoided).
     */
    private int calculateCurrentAvoidanceStreak(Habit habit, LocalDate endDate) {
        Set<LocalDate> completedDates = habit.getEntries().stream()
                .map(entry -> entry.getCompletedAt().toLocalDate())
                .collect(Collectors.toSet());

        if (completedDates.isEmpty()) {
            // No entries at all means no meaningful avoidance streak to calculate
            return 0;
        }

        int streak = 0;
        LocalDate checkDate = endDate;

        while (!completedDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    /**
     * For negative habits, finds the longest run of consecutive days
     * without any completions in the given period.
     */
    private int calculateLongestAvoidanceStreak(Habit habit, LocalDate start, LocalDate end) {
        Set<LocalDate> completedDates = habit.getEntries().stream()
                .map(entry -> entry.getCompletedAt().toLocalDate())
                .filter(date -> !date.isBefore(start) && !date.isAfter(end))
                .collect(Collectors.toSet());

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate checkDate = start;

        while (!checkDate.isAfter(end)) {
            if (!completedDates.contains(checkDate)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            checkDate = checkDate.plusDays(1);
        }

        return longestStreak;
    }
}
