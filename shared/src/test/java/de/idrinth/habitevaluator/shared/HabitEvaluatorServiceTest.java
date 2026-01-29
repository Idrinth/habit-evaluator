package de.idrinth.habitevaluator.shared;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HabitEvaluatorServiceTest {

    private HabitEvaluatorService evaluatorService;

    @BeforeEach
    void setUp() {
        evaluatorService = new HabitEvaluatorService();
    }

    @Test
    void testEvaluateWithNoEntries() {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 7);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);

        assertEquals(0, evaluation.getTotalEntries());
        assertEquals(7, evaluation.getTargetEntries());
        assertEquals(0.0, evaluation.getCompletionRate());
        assertFalse(evaluation.isOnTrack());
    }

    @Test
    void testEvaluateWithFullCompletion() {
        Habit habit = new Habit("Exercise", "Daily workout");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 7);

        for (int i = 0; i < 7; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(start.plusDays(i).atTime(10, 0));
            habit.addEntry(entry);
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);

        assertEquals(7, evaluation.getTotalEntries());
        assertEquals(7, evaluation.getTargetEntries());
        assertEquals(1.0, evaluation.getCompletionRate());
        assertTrue(evaluation.isOnTrack());
    }

    @Test
    void testStreakCalculation() {
        Habit habit = new Habit("Reading", "Read every day");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate today = LocalDate.now();

        // Create a 5-day streak ending today
        for (int i = 4; i >= 0; i--) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(today.minusDays(i).atTime(10, 0));
            habit.addEntry(entry);
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, today.minusDays(6), today);

        assertEquals(5, evaluation.getCurrentStreak());
        assertEquals(5, evaluation.getLongestStreak());
    }

    @Test
    void testEvaluateSetsPeriodDates() {
        Habit habit = new Habit("Test", "Test");
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);

        assertEquals(start, evaluation.getPeriodStart());
        assertEquals(end, evaluation.getPeriodEnd());
    }

    @Test
    void testEvaluateSetsHabitInfo() {
        Habit habit = new Habit("Meditation", "Daily mindfulness");
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 7);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);

        assertEquals(habit.getId(), evaluation.getHabitId());
        assertEquals("Meditation", evaluation.getHabitName());
    }

    @Test
    void testWeeklyFrequencyTargetEntries() {
        Habit habit = new Habit("Gym", "Go to gym");
        habit.setFrequencyType(FrequencyType.WEEKLY);
        habit.setTargetFrequency(3);

        // 14 days = 2 weeks
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 14);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(6, evaluation.getTargetEntries()); // 2 weeks * 3
    }

    @Test
    void testMonthlyFrequencyTargetEntries() {
        Habit habit = new Habit("Review", "Monthly review");
        habit.setFrequencyType(FrequencyType.MONTHLY);
        habit.setTargetFrequency(1);

        // 60 days = 2 months
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 3, 1);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(2, evaluation.getTargetEntries()); // ceil(60/30) * 1
    }

    @Test
    void testCompletionRateCappedAtOne() {
        Habit habit = new Habit("Exercise", "Daily");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 1);

        // Add 3 entries for a 1-day period (target = 1)
        for (int i = 0; i < 3; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(start.atTime(10 + i, 0));
            habit.addEntry(entry);
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(1.0, evaluation.getCompletionRate());
    }

    @Test
    void testEntriesOutsidePeriodNotCounted() {
        Habit habit = new Habit("Test", "Test");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 8);
        LocalDate end = LocalDate.of(2024, 1, 14);

        // Entry before period
        HabitEntry before = new HabitEntry(habit.getId());
        before.setCompletedAt(LocalDate.of(2024, 1, 7).atTime(23, 59));
        habit.addEntry(before);

        // Entry in period
        HabitEntry during = new HabitEntry(habit.getId());
        during.setCompletedAt(LocalDate.of(2024, 1, 10).atTime(10, 0));
        habit.addEntry(during);

        // Entry after period
        HabitEntry after = new HabitEntry(habit.getId());
        after.setCompletedAt(LocalDate.of(2024, 1, 15).atTime(0, 1));
        habit.addEntry(after);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(1, evaluation.getTotalEntries());
    }

    @Test
    void testLongestStreakWithGap() {
        Habit habit = new Habit("Test", "Test");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 10);

        // 3-day streak: Jan 1-3
        for (int i = 0; i < 3; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(start.plusDays(i).atTime(10, 0));
            habit.addEntry(entry);
        }

        // Gap on Jan 4

        // 5-day streak: Jan 5-9
        for (int i = 4; i < 9; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(start.plusDays(i).atTime(10, 0));
            habit.addEntry(entry);
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(5, evaluation.getLongestStreak());
    }

    @Test
    void testCurrentStreakZeroWhenEndDateNotCompleted() {
        Habit habit = new Habit("Test", "Test");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 7);

        // Entries on Jan 1-3 only, end date is Jan 7
        for (int i = 0; i < 3; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(start.plusDays(i).atTime(10, 0));
            habit.addEntry(entry);
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(0, evaluation.getCurrentStreak());
        assertEquals(3, evaluation.getLongestStreak());
    }

    @Test
    void testPartialCompletionRate() {
        Habit habit = new Habit("Exercise", "Daily");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 10);

        // Add 5 entries over 10 days
        for (int i = 0; i < 5; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(start.plusDays(i * 2).atTime(10, 0));
            habit.addEntry(entry);
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(5, evaluation.getTotalEntries());
        assertEquals(10, evaluation.getTargetEntries());
        assertEquals(0.5, evaluation.getCompletionRate(), 0.001);
        assertFalse(evaluation.isOnTrack());
    }

    @Test
    void testDailyFrequencyWithHigherTarget() {
        Habit habit = new Habit("Water", "Drink water");
        habit.setFrequencyType(FrequencyType.DAILY);
        habit.setTargetFrequency(3);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 7);

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        assertEquals(21, evaluation.getTargetEntries()); // 7 days * 3
    }
}
