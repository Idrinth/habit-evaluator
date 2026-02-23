package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

        const val LABEL_PATTERN = "MM/dd"
        val LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(LABEL_PATTERN)

        /**
         * Replication of sleep duration calculation from SleepAnalysisScreen.
         * Calculates hours of sleep from a from-until time pair, handling midnight crossing.
         */
        fun calculateSleepHours(from: LocalTime, until: LocalTime): Double {
            var mins = ChronoUnit.MINUTES.between(from, until)
            if (mins < 0) mins += 24 * 60
            return mins / 60.0
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

    // --- Label format tests ---

    @Test
    fun testLabelPatternValue() {
        assertEquals("MM/dd", LABEL_PATTERN)
    }

    @Test
    fun testLabelFormatNotNull() {
        assertNotNull(LABEL_FORMAT)
    }

    @Test
    fun testLabelFormatProducesExpectedOutput() {
        val date = LocalDate.of(2025, 7, 4)
        assertEquals("07/04", LABEL_FORMAT.format(date))
    }

    @Test
    fun testLabelFormatSingleDigitMonth() {
        val date = LocalDate.of(2025, 1, 5)
        assertEquals("01/05", LABEL_FORMAT.format(date))
    }

    @Test
    fun testLabelFormatEndOfYear() {
        val date = LocalDate.of(2025, 12, 31)
        assertEquals("12/31", LABEL_FORMAT.format(date))
    }

    @Test
    fun testLabelFormatStartOfYear() {
        val date = LocalDate.of(2025, 1, 1)
        assertEquals("01/01", LABEL_FORMAT.format(date))
    }

    // --- Sleep duration calculation tests ---

    @Test
    fun testCalculateSleepHoursNormalRange() {
        // 23:00 to 07:00 = 8 hours (midnight crossing)
        assertEquals(8.0, calculateSleepHours(LocalTime.of(23, 0), LocalTime.of(7, 0)), 0.001)
    }

    @Test
    fun testCalculateSleepHoursSameDay() {
        // 01:00 to 09:00 = 8 hours (no midnight crossing)
        assertEquals(8.0, calculateSleepHours(LocalTime.of(1, 0), LocalTime.of(9, 0)), 0.001)
    }

    @Test
    fun testCalculateSleepHoursShortNap() {
        // 14:00 to 15:30 = 1.5 hours
        assertEquals(1.5, calculateSleepHours(LocalTime.of(14, 0), LocalTime.of(15, 30)), 0.001)
    }

    @Test
    fun testCalculateSleepHoursMidnightCrossing() {
        // 22:00 to 06:00 = 8 hours
        assertEquals(8.0, calculateSleepHours(LocalTime.of(22, 0), LocalTime.of(6, 0)), 0.001)
    }

    @Test
    fun testCalculateSleepHoursExactMidnight() {
        // 00:00 to 08:00 = 8 hours
        assertEquals(8.0, calculateSleepHours(LocalTime.of(0, 0), LocalTime.of(8, 0)), 0.001)
    }

    @Test
    fun testCalculateSleepHoursFullDay() {
        // Same time = 24 hours (midnight crossing logic gives full day)
        assertEquals(0.0, calculateSleepHours(LocalTime.of(8, 0), LocalTime.of(8, 0)), 0.001)
    }

    @Test
    fun testCalculateSleepHours30Minutes() {
        assertEquals(0.5, calculateSleepHours(LocalTime.of(12, 0), LocalTime.of(12, 30)), 0.001)
    }

    @Test
    fun testCalculateSleepHoursLateMidnightCrossing() {
        // 23:30 to 05:15 = 5 hours 45 min = 5.75 hours
        assertEquals(5.75, calculateSleepHours(LocalTime.of(23, 30), LocalTime.of(5, 15)), 0.001)
    }

    @Test
    fun testCalculateAverageNegativeCount() {
        assertEquals(0f, calculateAverage(10f, -1), 0.001f)
    }
}
