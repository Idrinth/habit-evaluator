package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PredictedHabitScoreTest {

    @Test
    void testDefaultConstructor() {
        PredictedHabitScore score = new PredictedHabitScore();
        assertNull(score.getHabitId());
        assertNull(score.getHabitName());
        assertEquals(0, score.getCurrentCompletionCount());
        assertEquals(0, score.getCurrentScore());
        assertEquals(0, score.getPredictedCompletionCount());
        assertEquals(0, score.getPredictedScore());
        assertEquals(0.0, score.getDailyRate());
    }

    @Test
    void testFullConstructor() {
        PredictedHabitScore score = new PredictedHabitScore(
                "h1", "Exercise", 3, 4, 7, 8, 1.5);
        assertEquals("h1", score.getHabitId());
        assertEquals("Exercise", score.getHabitName());
        assertEquals(3, score.getCurrentCompletionCount());
        assertEquals(4, score.getCurrentScore());
        assertEquals(7, score.getPredictedCompletionCount());
        assertEquals(8, score.getPredictedScore());
        assertEquals(1.5, score.getDailyRate(), 0.001);
    }

    @Test
    void testSetHabitId() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setHabitId("h2");
        assertEquals("h2", score.getHabitId());
    }

    @Test
    void testSetHabitName() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setHabitName("Reading");
        assertEquals("Reading", score.getHabitName());
    }

    @Test
    void testSetCategoryId() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setCategoryId("cat-1");
        assertEquals("cat-1", score.getCategoryId());
    }

    @Test
    void testSetCurrentCompletionCount() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setCurrentCompletionCount(5);
        assertEquals(5, score.getCurrentCompletionCount());
    }

    @Test
    void testSetCurrentScore() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setCurrentScore(10);
        assertEquals(10, score.getCurrentScore());
    }

    @Test
    void testSetPredictedCompletionCount() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setPredictedCompletionCount(12);
        assertEquals(12, score.getPredictedCompletionCount());
    }

    @Test
    void testSetPredictedScore() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setPredictedScore(16);
        assertEquals(16, score.getPredictedScore());
    }

    @Test
    void testSetDailyRate() {
        PredictedHabitScore score = new PredictedHabitScore();
        score.setDailyRate(2.5);
        assertEquals(2.5, score.getDailyRate(), 0.001);
    }
}
