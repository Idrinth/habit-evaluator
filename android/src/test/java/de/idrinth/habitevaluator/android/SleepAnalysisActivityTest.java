package de.idrinth.habitevaluator.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class SleepAnalysisActivityTest {

    @Test
    public void testCalculateInterruptionsZeroSegments() {
        assertEquals(0, SleepAnalysisActivity.calculateInterruptions(0));
    }

    @Test
    public void testCalculateInterruptionsOneSegment() {
        // A single sleep segment means no interruptions
        assertEquals(0, SleepAnalysisActivity.calculateInterruptions(1));
    }

    @Test
    public void testCalculateInterruptionsTwoSegments() {
        // Two sleep segments means one interruption (woke up and went back to sleep)
        assertEquals(1, SleepAnalysisActivity.calculateInterruptions(2));
    }

    @Test
    public void testCalculateInterruptionsThreeSegments() {
        assertEquals(2, SleepAnalysisActivity.calculateInterruptions(3));
    }

    @Test
    public void testCalculateInterruptionsManySegments() {
        assertEquals(9, SleepAnalysisActivity.calculateInterruptions(10));
    }

    @Test
    public void testCalculateInterruptionsNeverNegative() {
        // Even with negative input (shouldn't happen in practice), result is non-negative
        assertTrue(SleepAnalysisActivity.calculateInterruptions(-1) >= 0);
    }

    @Test
    public void testCalculateAverageWithData() {
        assertEquals(5.0f, SleepAnalysisActivity.calculateAverage(25f, 5), 0.001f);
    }

    @Test
    public void testCalculateAverageWithZeroCount() {
        assertEquals(0f, SleepAnalysisActivity.calculateAverage(100f, 0), 0.001f);
    }

    @Test
    public void testCalculateAverageWithZeroTotal() {
        assertEquals(0f, SleepAnalysisActivity.calculateAverage(0f, 5), 0.001f);
    }

    @Test
    public void testCalculateAverageWithBothZero() {
        assertEquals(0f, SleepAnalysisActivity.calculateAverage(0f, 0), 0.001f);
    }

    @Test
    public void testCalculateAveragePrecision() {
        // 7 hours over 3 days should give a precise average
        assertEquals(7f / 3f, SleepAnalysisActivity.calculateAverage(7f, 3), 0.001f);
    }

    @Test
    public void testCalculateAverageSingleDay() {
        assertEquals(8.5f, SleepAnalysisActivity.calculateAverage(8.5f, 1), 0.001f);
    }
}
