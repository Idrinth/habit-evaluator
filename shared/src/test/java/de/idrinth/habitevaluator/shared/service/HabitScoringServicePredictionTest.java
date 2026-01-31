package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.PredictedHabitScore;
import de.idrinth.habitevaluator.shared.model.PredictedWeeklyScore;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HabitScoringServicePredictionTest {

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
    void testPredictHabitScoreNoEntries() {
        Habit habit = new Habit("Exercise", "Daily workout");
        LocalDate weekStart = LocalDate.of(2024, 1, 1); // Monday
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);   // Sunday
        LocalDateTime now = LocalDate.of(2024, 1, 4).atTime(12, 0); // Thursday noon

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals(0, prediction.getCurrentCompletionCount());
        assertEquals(0, prediction.getCurrentScore());
        assertEquals(0, prediction.getPredictedCompletionCount());
        assertEquals(0, prediction.getPredictedScore());
        assertEquals(0.0, prediction.getDailyRate(), 0.001);
    }

    @Test
    void testPredictHabitScoreMidweekExtrapolation() {
        // 3 entries in first 3 days (Mon-Wed), predicting from Wednesday noon
        LocalDate weekStart = LocalDate.of(2024, 1, 1); // Monday
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);   // Sunday
        Habit habit = createHabitWithEntries("Exercise", weekStart, 3);
        LocalDateTime now = LocalDate.of(2024, 1, 3).atTime(12, 0); // Wednesday noon

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals(3, prediction.getCurrentCompletionCount());
        assertEquals(2, prediction.getCurrentScore()); // default rule: 2 completions = 2 points
        // Rate: 3 completions in 60 hours (~1.2/day), remaining ~108 hours
        // Predicted additional: ~5.4, total ~8
        assertTrue(prediction.getPredictedCompletionCount() >= 7,
                "Should predict at least 7 completions when averaging 1/day over 3 days");
        assertEquals(8, prediction.getPredictedScore()); // default rule: 7+ = 8 points
    }

    @Test
    void testPredictHabitScoreEndOfWeek() {
        // Week is over, prediction should equal current score
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        Habit habit = createHabitWithEntries("Exercise", weekStart, 4);
        LocalDateTime now = LocalDate.of(2024, 1, 8).atTime(0, 0); // Monday after

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals(prediction.getCurrentCompletionCount(), prediction.getPredictedCompletionCount());
        assertEquals(prediction.getCurrentScore(), prediction.getPredictedScore());
    }

    @Test
    void testPredictHabitScoreFirstDayMorning() {
        // Monday early with 1 entry, should extrapolate aggressively
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        Habit habit = new Habit("Reading", "Read daily");
        HabitEntry entry = new HabitEntry(habit.getId());
        entry.setCompletedAt(weekStart.atTime(8, 0));
        habit.addEntry(entry);

        LocalDateTime now = weekStart.atTime(9, 0); // Monday 9 AM (9 hours elapsed)

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals(1, prediction.getCurrentCompletionCount());
        assertTrue(prediction.getPredictedCompletionCount() > 1,
                "Should predict more than 1 completion when rate is high early on");
    }

    @Test
    void testPredictHabitScoreWithCustomScoringRule() {
        Habit habit = new Habit("Meditation", "Morning meditation");
        habit.setScoringRule(new ScoringRule("Custom", 1, 2, 3, 5));

        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        for (int i = 0; i < 3; i++) {
            HabitEntry entry = new HabitEntry(habit.getId());
            entry.setCompletedAt(weekStart.plusDays(i).atTime(8, 0));
            habit.addEntry(entry);
        }

        // Wednesday evening - 3 entries in ~3 days
        LocalDateTime now = LocalDate.of(2024, 1, 3).atTime(20, 0);

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals(3, prediction.getCurrentCompletionCount());
        assertEquals(4, prediction.getCurrentScore()); // custom rule: 3+ = 4 points
        assertTrue(prediction.getPredictedCompletionCount() >= 5,
                "Should predict hitting 8-point threshold with consistent 1/day rate");
        assertEquals(8, prediction.getPredictedScore()); // custom rule: 5+ = 8 points
    }

    @Test
    void testPredictHabitScoreWithNullScoringRule() {
        Habit habit = new Habit("Test", "Test");
        habit.setScoringRule(null);

        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        HabitEntry entry = new HabitEntry(habit.getId());
        entry.setCompletedAt(weekStart.atTime(10, 0));
        habit.addEntry(entry);

        LocalDateTime now = weekStart.atTime(12, 0);

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals(1, prediction.getCurrentScore());
        // Should use default scoring rule
        assertTrue(prediction.getPredictedScore() >= 1);
    }

    @Test
    void testPredictHabitScoreSetsCategory() {
        Habit habit = new Habit("Exercise", "Workout");
        habit.setCategoryId("cat-123");
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        LocalDateTime now = weekStart.atTime(12, 0);

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertEquals("cat-123", prediction.getCategoryId());
    }

    @Test
    void testPredictWeeklyScoreEmpty() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        LocalDateTime now = LocalDate.of(2024, 1, 4).atTime(12, 0);

        PredictedWeeklyScore prediction = scoringService.predictWeeklyScore(List.of(), weekStart, weekEnd, now);

        assertEquals(0, prediction.getCurrentTotalScore());
        assertEquals(0, prediction.getPredictedTotalScore());
        assertTrue(prediction.getHabitPredictions().isEmpty());
        assertEquals(weekStart, prediction.getWeekStart());
        assertEquals(weekEnd, prediction.getWeekEnd());
    }

    @Test
    void testPredictWeeklyScoreMultipleHabits() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        LocalDateTime now = LocalDate.of(2024, 1, 4).atTime(12, 0);

        Habit habit1 = createHabitWithEntries("Exercise", weekStart, 3);
        Habit habit2 = createHabitWithEntries("Reading", weekStart, 2);

        PredictedWeeklyScore prediction = scoringService.predictWeeklyScore(
                List.of(habit1, habit2), weekStart, weekEnd, now);

        assertEquals(2, prediction.getHabitPredictions().size());
        assertTrue(prediction.getPredictedTotalScore() >= prediction.getCurrentTotalScore(),
                "Predicted score should be >= current score when there are remaining days");
    }

    @Test
    void testPredictWeeklyScoreDaysElapsedAndRemaining() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1); // Monday
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);   // Sunday
        LocalDateTime now = LocalDate.of(2024, 1, 4).atTime(12, 0); // Thursday noon

        PredictedWeeklyScore prediction = scoringService.predictWeeklyScore(List.of(), weekStart, weekEnd, now);

        assertEquals(4, prediction.getDaysElapsed());  // Mon, Tue, Wed, Thu
        assertEquals(3, prediction.getDaysRemaining()); // Fri, Sat, Sun
    }

    @Test
    void testPredictWeeklyScoreCategoryFiltering() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        LocalDateTime now = LocalDate.of(2024, 1, 4).atTime(12, 0);

        Habit habit1 = createHabitWithEntries("Exercise", weekStart, 3);
        habit1.setCategoryId("fitness");
        Habit habit2 = createHabitWithEntries("Reading", weekStart, 2);
        habit2.setCategoryId("learning");

        PredictedWeeklyScore prediction = scoringService.predictWeeklyScore(
                List.of(habit1, habit2), weekStart, weekEnd, now);

        int fitnessScore = prediction.getPredictedScoreByCategory("fitness");
        int learningScore = prediction.getPredictedScoreByCategory("learning");
        assertEquals(prediction.getPredictedTotalScore(), fitnessScore + learningScore);
    }

    @Test
    void testPredictHabitScoreDailyRateIsPositiveWithEntries() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        Habit habit = createHabitWithEntries("Exercise", weekStart, 3);
        LocalDateTime now = LocalDate.of(2024, 1, 3).atTime(12, 0);

        PredictedHabitScore prediction = scoringService.predictHabitScore(habit, weekStart, weekEnd, now);

        assertTrue(prediction.getDailyRate() > 0, "Daily rate should be positive when there are entries");
    }

    @Test
    void testPredictWeeklyScoreWeekNumberAndYear() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);
        LocalDateTime now = LocalDate.of(2024, 1, 4).atTime(12, 0);

        PredictedWeeklyScore prediction = scoringService.predictWeeklyScore(List.of(), weekStart, weekEnd, now);

        assertEquals(1, prediction.getWeekNumber());
        assertEquals(2024, prediction.getYear());
    }
}
