package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SleepAnalysisActivityTest {

    @Test
    void testCalculateInterruptionsZeroSegments() {
        assertEquals(0, SleepAnalysisActivity.calculateInterruptions(0));
    }

    @Test
    void testCalculateInterruptionsOneSegment() {
        // A single sleep segment means no interruptions
        assertEquals(0, SleepAnalysisActivity.calculateInterruptions(1));
    }

    @Test
    void testCalculateInterruptionsTwoSegments() {
        // Two sleep segments means one interruption (woke up and went back to sleep)
        assertEquals(1, SleepAnalysisActivity.calculateInterruptions(2));
    }

    @Test
    void testCalculateInterruptionsThreeSegments() {
        assertEquals(2, SleepAnalysisActivity.calculateInterruptions(3));
    }

    @Test
    void testCalculateInterruptionsManySegments() {
        assertEquals(9, SleepAnalysisActivity.calculateInterruptions(10));
    }

    @Test
    void testCalculateInterruptionsNeverNegative() {
        // Even with negative input (shouldn't happen in practice), result is non-negative
        assertTrue(SleepAnalysisActivity.calculateInterruptions(-1) >= 0);
    }

    @Test
    void testCalculateAverageWithData() {
        assertEquals(5.0f, SleepAnalysisActivity.calculateAverage(25f, 5), 0.001f);
    }

    @Test
    void testCalculateAverageWithZeroCount() {
        assertEquals(0f, SleepAnalysisActivity.calculateAverage(100f, 0), 0.001f);
    }

    @Test
    void testCalculateAverageWithZeroTotal() {
        assertEquals(0f, SleepAnalysisActivity.calculateAverage(0f, 5), 0.001f);
    }

    @Test
    void testCalculateAverageWithBothZero() {
        assertEquals(0f, SleepAnalysisActivity.calculateAverage(0f, 0), 0.001f);
    }

    @Test
    void testCalculateAveragePrecision() {
        // 7 hours over 3 days should give a precise average
        assertEquals(7f / 3f, SleepAnalysisActivity.calculateAverage(7f, 3), 0.001f);
    }

    @Test
    void testCalculateAverageSingleDay() {
        assertEquals(8.5f, SleepAnalysisActivity.calculateAverage(8.5f, 1), 0.001f);
    }
}
