package de.idrinth.habitevaluator.shared;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
