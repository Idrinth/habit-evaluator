package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationTest {

    @Test
    void testDefaultConstructor() {
        Evaluation eval = new Evaluation();
        assertNull(eval.getHabitId());
        assertNull(eval.getHabitName());
    }

    @Test
    void testParameterizedConstructor() {
        Evaluation eval = new Evaluation("habit-1", "Exercise");
        assertEquals("habit-1", eval.getHabitId());
        assertEquals("Exercise", eval.getHabitName());
    }

    @Test
    void testIsOnTrackReturnsTrueAt80Percent() {
        Evaluation eval = new Evaluation();
        eval.setCompletionRate(0.8);
        assertTrue(eval.isOnTrack());
    }

    @Test
    void testIsOnTrackReturnsTrueAbove80Percent() {
        Evaluation eval = new Evaluation();
        eval.setCompletionRate(1.0);
        assertTrue(eval.isOnTrack());
    }

    @Test
    void testIsOnTrackReturnsFalseBelow80Percent() {
        Evaluation eval = new Evaluation();
        eval.setCompletionRate(0.79);
        assertFalse(eval.isOnTrack());
    }

    @Test
    void testIsOnTrackReturnsFalseAtZero() {
        Evaluation eval = new Evaluation();
        eval.setCompletionRate(0.0);
        assertFalse(eval.isOnTrack());
    }

    @Test
    void testSettersAndGetters() {
        Evaluation eval = new Evaluation();
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 7);

        eval.setHabitId("id-1");
        eval.setHabitName("Reading");
        eval.setPeriodStart(start);
        eval.setPeriodEnd(end);
        eval.setTotalEntries(5);
        eval.setTargetEntries(7);
        eval.setCompletionRate(0.714);
        eval.setCurrentStreak(3);
        eval.setLongestStreak(5);

        assertEquals("id-1", eval.getHabitId());
        assertEquals("Reading", eval.getHabitName());
        assertEquals(start, eval.getPeriodStart());
        assertEquals(end, eval.getPeriodEnd());
        assertEquals(5, eval.getTotalEntries());
        assertEquals(7, eval.getTargetEntries());
        assertEquals(0.714, eval.getCompletionRate());
        assertEquals(3, eval.getCurrentStreak());
        assertEquals(5, eval.getLongestStreak());
    }
}
