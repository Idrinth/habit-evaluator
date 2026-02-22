package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for sleep analysis calculation logic equivalent to what SleepAnalysisActivity provided.
 */
class SleepAnalysisScreenTest {

    companion object {
        /**
         * Replication of calculateInterruptions logic from SleepAnalysisActivity.
         * A single sleep segment means no interruptions; each additional segment is one interruption.
         * Returns max(0, segments - 1).
         */
        fun calculateInterruptions(segments: Int): Int = maxOf(0, segments - 1)

        /**
         * Replication of calculateAverage logic from SleepAnalysisActivity.
         * Returns 0 when count is <= 0.
         */
        fun calculateAverage(total: Float, count: Int): Float {
            return if (count <= 0) 0f else total / count
        }
    }

    @Test
    fun testCalculateInterruptionsZeroSegments() {
        assertEquals(0, calculateInterruptions(0))
    }

    @Test
    fun testCalculateInterruptionsOneSegment() {
        // A single sleep segment means no interruptions
        assertEquals(0, calculateInterruptions(1))
    }

    @Test
    fun testCalculateInterruptionsTwoSegments() {
        // Two sleep segments means one interruption (woke up and went back to sleep)
        assertEquals(1, calculateInterruptions(2))
    }

    @Test
    fun testCalculateInterruptionsThreeSegments() {
        assertEquals(2, calculateInterruptions(3))
    }

    @Test
    fun testCalculateInterruptionsManySegments() {
        assertEquals(9, calculateInterruptions(10))
    }

    @Test
    fun testCalculateInterruptionsNeverNegative() {
        // Even with negative input (shouldn't happen in practice), result is non-negative
        assertTrue(calculateInterruptions(-1) >= 0)
    }

    @Test
    fun testCalculateAverageWithData() {
        assertEquals(5.0f, calculateAverage(25f, 5), 0.001f)
    }

    @Test
    fun testCalculateAverageWithZeroCount() {
        assertEquals(0f, calculateAverage(100f, 0), 0.001f)
    }

    @Test
    fun testCalculateAverageWithZeroTotal() {
        assertEquals(0f, calculateAverage(0f, 5), 0.001f)
    }

    @Test
    fun testCalculateAverageWithBothZero() {
        assertEquals(0f, calculateAverage(0f, 0), 0.001f)
    }

    @Test
    fun testCalculateAveragePrecision() {
        // 7 hours over 3 days should give a precise average
        assertEquals(7f / 3f, calculateAverage(7f, 3), 0.001f)
    }

    @Test
    fun testCalculateAverageSingleDay() {
        assertEquals(8.5f, calculateAverage(8.5f, 1), 0.001f)
    }
}
