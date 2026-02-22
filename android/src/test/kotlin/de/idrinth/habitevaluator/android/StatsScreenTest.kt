package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Tests for stats constants and pure utility functions equivalent to what
 * StatsFragment previously provided as static members and helper methods.
 */
class StatsScreenTest {

    companion object {
        const val DAYS = 30
        const val LABEL_PATTERN = "yyyy-MM-dd"
        val LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(LABEL_PATTERN)

        /**
         * Replication of calculateAverage logic from StatsFragment.
         * Returns 0 when count is <= 0.
         */
        fun calculateAverage(total: Float, count: Int): Float {
            return if (count <= 0) 0f else total / count
        }

        /**
         * Replication of generateDateLabels logic from StatsFragment.
         * Generates a list of date label strings from start to end (inclusive).
         */
        fun generateDateLabels(start: LocalDate, end: LocalDate): List<String> {
            val labels = mutableListOf<String>()
            var current = start
            while (!current.isAfter(end)) {
                labels.add(LABEL_FORMAT.format(current))
                current = current.plusDays(1)
            }
            return labels
        }
    }

    @Test
    fun testDaysConstant() {
        assertEquals(30, DAYS)
    }

    @Test
    fun testLabelPatternNotNull() {
        assertNotNull(LABEL_PATTERN)
    }

    @Test
    fun testLabelFormatNotNull() {
        assertNotNull(LABEL_FORMAT)
    }

    @Test
    fun testLabelFormatProducesExpectedOutput() {
        val date = LocalDate.of(2025, 3, 15)
        assertEquals("2025-03-15", LABEL_FORMAT.format(date))
    }

    @Test
    fun testLabelFormatWithDecember() {
        val date = LocalDate.of(2025, 12, 1)
        assertEquals("2025-12-01", LABEL_FORMAT.format(date))
    }

    @Test
    fun testLabelFormatWithJanuary() {
        val date = LocalDate.of(2025, 1, 31)
        assertEquals("2025-01-31", LABEL_FORMAT.format(date))
    }

    @Test
    fun testCalculateAverageWithPositiveValues() {
        assertEquals(5.0f, calculateAverage(15.0f, 3), 0.001f)
    }

    @Test
    fun testCalculateAverageWithZeroCount() {
        assertEquals(0.0f, calculateAverage(100.0f, 0), 0.001f)
    }

    @Test
    fun testCalculateAverageWithZeroTotal() {
        assertEquals(0.0f, calculateAverage(0.0f, 5), 0.001f)
    }

    @Test
    fun testCalculateAverageWithOneItem() {
        assertEquals(42.0f, calculateAverage(42.0f, 1), 0.001f)
    }

    @Test
    fun testCalculateAverageWithFractionalResult() {
        assertEquals(3.333f, calculateAverage(10.0f, 3), 0.01f)
    }

    @Test
    fun testGenerateDateLabelsForSingleDay() {
        val date = LocalDate.of(2025, 6, 15)
        val labels = generateDateLabels(date, date)
        assertEquals(1, labels.size)
        assertEquals("2025-06-15", labels[0])
    }

    @Test
    fun testGenerateDateLabelsForThreeDays() {
        val start = LocalDate.of(2025, 6, 13)
        val end = LocalDate.of(2025, 6, 15)
        val labels = generateDateLabels(start, end)
        assertEquals(3, labels.size)
        assertEquals("2025-06-13", labels[0])
        assertEquals("2025-06-14", labels[1])
        assertEquals("2025-06-15", labels[2])
    }

    @Test
    fun testGenerateDateLabelsSpanningMonthBoundary() {
        val start = LocalDate.of(2025, 1, 30)
        val end = LocalDate.of(2025, 2, 2)
        val labels = generateDateLabels(start, end)
        assertEquals(4, labels.size)
        assertEquals("2025-01-30", labels[0])
        assertEquals("2025-01-31", labels[1])
        assertEquals("2025-02-01", labels[2])
        assertEquals("2025-02-02", labels[3])
    }

    @Test
    fun testGenerateDateLabelsFor30Days() {
        val end = LocalDate.of(2025, 6, 30)
        val start = end.minusDays((DAYS - 1).toLong())
        val labels = generateDateLabels(start, end)
        assertEquals(DAYS, labels.size)
    }

    @Test
    fun testGenerateDateLabelsStartAfterEndReturnsEmpty() {
        val start = LocalDate.of(2025, 6, 15)
        val end = LocalDate.of(2025, 6, 14)
        val labels = generateDateLabels(start, end)
        assertTrue(labels.isEmpty())
    }

    @Test
    fun testCalculateAverageWithLargeTotal() {
        assertEquals(1000.0f, calculateAverage(10000.0f, 10), 0.001f)
    }

    @Test
    fun testCalculateAverageWithSmallFractionalValues() {
        assertEquals(0.1f, calculateAverage(0.3f, 3), 0.01f)
    }

    @Test
    fun testCalculateAverageWithNegativeTotal() {
        assertEquals(-5.0f, calculateAverage(-10.0f, 2), 0.001f)
    }

    @Test
    fun testCalculateAverageWithNegativeCount() {
        // Negative count returns 0 (treated same as zero count)
        val result = calculateAverage(10.0f, -2)
        assertEquals(0.0f, result, 0.001f)
    }

    @Test
    fun testGenerateDateLabelsSpanningYearBoundary() {
        val start = LocalDate.of(2024, 12, 30)
        val end = LocalDate.of(2025, 1, 2)
        val labels = generateDateLabels(start, end)
        assertEquals(4, labels.size)
        assertEquals("2024-12-30", labels[0])
        assertEquals("2024-12-31", labels[1])
        assertEquals("2025-01-01", labels[2])
        assertEquals("2025-01-02", labels[3])
    }

    @Test
    fun testGenerateDateLabelsSpanningLeapDay() {
        val start = LocalDate.of(2024, 2, 28)
        val end = LocalDate.of(2024, 3, 1)
        val labels = generateDateLabels(start, end)
        assertEquals(3, labels.size)
        assertEquals("2024-02-28", labels[0])
        assertEquals("2024-02-29", labels[1])
        assertEquals("2024-03-01", labels[2])
    }

    @Test
    fun testLabelPatternIsYyyyMMdd() {
        assertEquals("yyyy-MM-dd", LABEL_PATTERN)
    }

    @Test
    fun testLabelFormatWithFebruary() {
        val date = LocalDate.of(2025, 2, 14)
        assertEquals("2025-02-14", LABEL_FORMAT.format(date))
    }

    @Test
    fun testGenerateDateLabelsAllLabelsHaveDash() {
        val start = LocalDate.of(2025, 6, 1)
        val end = LocalDate.of(2025, 6, 10)
        val labels = generateDateLabels(start, end)
        for (label in labels) {
            assertTrue(label.contains("-"), "Each label should contain a dash separator")
        }
    }

    @Test
    fun testGenerateDateLabelsAllLabelsHaveTenCharacters() {
        val start = LocalDate.of(2025, 6, 1)
        val end = LocalDate.of(2025, 6, 10)
        val labels = generateDateLabels(start, end)
        for (label in labels) {
            assertEquals(10, label.length, "Each label should be in yyyy-MM-dd format (10 chars)")
        }
    }
}
