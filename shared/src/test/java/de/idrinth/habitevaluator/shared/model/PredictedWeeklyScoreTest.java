package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PredictedWeeklyScoreTest {

    @Test
    void testDefaultConstructor() {
        PredictedWeeklyScore score = new PredictedWeeklyScore();
        assertNotNull(score.getHabitPredictions());
        assertTrue(score.getHabitPredictions().isEmpty());
        assertNotNull(score.getCalculatedAt());
        assertEquals(0, score.getCurrentTotalScore());
        assertEquals(0, score.getPredictedTotalScore());
    }

    @Test
    void testFullConstructor() {
        LocalDate start = LocalDate.of(2026, 2, 2);
        LocalDate end = LocalDate.of(2026, 2, 8);
        PredictedWeeklyScore score = new PredictedWeeklyScore(start, end, 3, 4);

        assertEquals(start, score.getWeekStart());
        assertEquals(end, score.getWeekEnd());
        assertEquals(3, score.getDaysElapsed());
        assertEquals(4, score.getDaysRemaining());
        assertTrue(score.getWeekNumber() > 0);
        assertEquals(2026, score.getYear());
    }

    @Test
    void testAddHabitPredictionRecalculatesScores() {
        PredictedWeeklyScore score = new PredictedWeeklyScore(
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 8), 3, 4);

        PredictedHabitScore p1 = new PredictedHabitScore("h1", "A", 2, 4, 5, 8, 1.0);
        PredictedHabitScore p2 = new PredictedHabitScore("h2", "B", 1, 2, 3, 4, 0.5);

        score.addHabitPrediction(p1);
        assertEquals(4, score.getCurrentTotalScore());
        assertEquals(8, score.getPredictedTotalScore());

        score.addHabitPrediction(p2);
        assertEquals(6, score.getCurrentTotalScore());
        assertEquals(12, score.getPredictedTotalScore());
    }

    @Test
    void testSetHabitPredictionsRecalculates() {
        PredictedWeeklyScore score = new PredictedWeeklyScore();
        List<PredictedHabitScore> predictions = new ArrayList<>();
        PredictedHabitScore p = new PredictedHabitScore("h1", "A", 2, 3, 5, 7, 1.0);
        predictions.add(p);

        score.setHabitPredictions(predictions);
        assertEquals(3, score.getCurrentTotalScore());
        assertEquals(7, score.getPredictedTotalScore());
    }

    @Test
    void testGetPredictedScoreByCategory() {
        PredictedWeeklyScore score = new PredictedWeeklyScore();

        PredictedHabitScore p1 = new PredictedHabitScore("h1", "A", 2, 4, 5, 8, 1.0);
        p1.setCategoryId("cat-1");
        PredictedHabitScore p2 = new PredictedHabitScore("h2", "B", 1, 2, 3, 4, 0.5);
        p2.setCategoryId("cat-2");
        PredictedHabitScore p3 = new PredictedHabitScore("h3", "C", 1, 1, 2, 2, 0.3);
        p3.setCategoryId("cat-1");

        score.addHabitPrediction(p1);
        score.addHabitPrediction(p2);
        score.addHabitPrediction(p3);

        assertEquals(10, score.getPredictedScoreByCategory("cat-1"));
        assertEquals(4, score.getPredictedScoreByCategory("cat-2"));
        assertEquals(0, score.getPredictedScoreByCategory("nonexistent"));
    }

    @Test
    void testSetters() {
        PredictedWeeklyScore score = new PredictedWeeklyScore();
        LocalDate start = LocalDate.of(2026, 1, 6);
        LocalDate end = LocalDate.of(2026, 1, 12);

        score.setWeekStart(start);
        score.setWeekEnd(end);
        score.setWeekNumber(2);
        score.setYear(2026);
        score.setDaysElapsed(5);
        score.setDaysRemaining(2);
        score.setCurrentTotalScore(10);
        score.setPredictedTotalScore(20);

        assertEquals(start, score.getWeekStart());
        assertEquals(end, score.getWeekEnd());
        assertEquals(2, score.getWeekNumber());
        assertEquals(2026, score.getYear());
        assertEquals(5, score.getDaysElapsed());
        assertEquals(2, score.getDaysRemaining());
        assertEquals(10, score.getCurrentTotalScore());
        assertEquals(20, score.getPredictedTotalScore());
    }
}
