package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.HabitScore;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.WeeklyScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HabitScoringServiceTest {

    private HabitScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new HabitScoringService();
    }

    private Habit createHabitWithEntries(String name, LocalDate weekStart, int entryCount) {
        Habit habit = new Habit(name, "Test habit");
        for (int i = 0; i < entryCount; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(weekStart.plusDays(i % 7).atTime(10, 0));
            habit.addEntry(entry);
        }
        return habit;
    }

    @Test
    void testCalculateHabitScoreNoEntries() {
        Habit habit = new Habit("Exercise", "Daily workout");
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        HabitScore score = scoringService.calculateHabitScore(habit, weekStart, weekEnd);

        assertEquals(0, score.getCompletionCount());
        assertEquals(0, score.getScore());
        assertEquals(habit.getId(), score.getHabitId());
        assertEquals("Exercise", score.getHabitName());
    }

    @Test
    void testCalculateHabitScoreWithEntries() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        Habit habit = createHabitWithEntries("Exercise", weekStart, 4);

        HabitScore score = scoringService.calculateHabitScore(habit, weekStart, weekEnd);

        assertEquals(4, score.getCompletionCount());
        assertEquals(4, score.getScore());
    }

    @Test
    void testCalculateHabitScoreWithCustomScoringRule() {
        Habit habit = new Habit("Meditation", "Morning meditation");
        habit.setScoringRule(new ScoringRule("Custom", 1, 2, 3, 5));

        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        for (int i = 0; i < 3; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(weekStart.plusDays(i).atTime(8, 0));
            habit.addEntry(entry);
        }

        HabitScore score = scoringService.calculateHabitScore(habit, weekStart, weekEnd);
        assertEquals(3, score.getCompletionCount());
        assertEquals(4, score.getScore());
    }

    @Test
    void testCalculateHabitScoreWithNullScoringRule() {
        Habit habit = new Habit("Test", "Test");
        habit.setScoringRule(null);

        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        HabitEntry entry = new HabitEntry(habit.getId());
        entry.setCompletedAt(weekStart.atTime(10, 0));
        habit.addEntry(entry);

        HabitScore score = scoringService.calculateHabitScore(habit, weekStart, weekEnd);
        assertEquals(1, score.getScore());
    }

    @Test
    void testCalculateHabitScoreSetsCategory() {
        Habit habit = new Habit("Exercise", "Workout");
        habit.setCategoryId("cat-123");
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        HabitScore score = scoringService.calculateHabitScore(habit, weekStart, weekEnd);
        assertEquals("cat-123", score.getCategoryId());
    }

    @Test
    void testCalculateWeeklyScoreEmpty() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        WeeklyScore weeklyScore = scoringService.calculateWeeklyScore(List.of(), weekStart, weekEnd);

        assertEquals(0, weeklyScore.getTotalScore());
        assertTrue(weeklyScore.getHabitScores().isEmpty());
        assertEquals(weekStart, weeklyScore.getWeekStart());
        assertEquals(weekEnd, weeklyScore.getWeekEnd());
    }

    @Test
    void testCalculateWeeklyScoreMultipleHabits() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        Habit habit1 = createHabitWithEntries("Exercise", weekStart, 7);
        Habit habit2 = createHabitWithEntries("Reading", weekStart, 2);

        WeeklyScore weeklyScore = scoringService.calculateWeeklyScore(
                List.of(habit1, habit2), weekStart, weekEnd);

        assertEquals(2, weeklyScore.getHabitScores().size());
        assertEquals(10, weeklyScore.getTotalScore()); // 8 + 2
    }

    @Test
    void testCountCompletionsInPeriodFiltersCorrectly() {
        Habit habit = new Habit("Test", "Test");
        LocalDate weekStart = LocalDate.of(2024, 1, 8);
        LocalDate weekEnd = LocalDate.of(2024, 1, 14);

        // Entry before the period
        HabitEntry before = new HabitEntry(habit.getId());
        before.setCompletedAt(LocalDate.of(2024, 1, 7).atTime(23, 59));
        habit.addEntry(before);

        // Entry in the period
        HabitEntry during = new HabitEntry(habit.getId());
        during.setCompletedAt(LocalDate.of(2024, 1, 10).atTime(10, 0));
        habit.addEntry(during);

        // Entry after the period
        HabitEntry after = new HabitEntry(habit.getId());
        after.setCompletedAt(LocalDate.of(2024, 1, 15).atTime(0, 1));
        habit.addEntry(after);

        int count = scoringService.countCompletionsInPeriod(habit, weekStart, weekEnd);
        assertEquals(1, count);
    }

    @Test
    void testCountCompletionsIncludesBoundaryDates() {
        Habit habit = new Habit("Test", "Test");
        LocalDate start = LocalDate.of(2024, 1, 8);
        LocalDate end = LocalDate.of(2024, 1, 14);

        HabitEntry startEntry = new HabitEntry(habit.getId());
        startEntry.setCompletedAt(start.atTime(0, 0));
        habit.addEntry(startEntry);

        HabitEntry endEntry = new HabitEntry(habit.getId());
        endEntry.setCompletedAt(end.atTime(23, 59));
        habit.addEntry(endEntry);

        int count = scoringService.countCompletionsInPeriod(habit, start, end);
        assertEquals(2, count);
    }
}
