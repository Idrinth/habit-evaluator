package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatsFragmentTest {

    @Test
    void testDaysConstant() {
        assertEquals(30, StatsFragment.DAYS);
    }

    @Test
    void testLabelPatternNotNull() {
        assertNotNull(StatsFragment.LABEL_PATTERN);
    }

    @Test
    void testLabelFormatNotNull() {
        assertNotNull(StatsFragment.LABEL_FORMAT);
    }

    @Test
    void testLabelFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        assertEquals("2025-03-15", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    void testLabelFormatWithDecember() {
        LocalDate date = LocalDate.of(2025, 12, 1);
        assertEquals("2025-12-01", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    void testLabelFormatWithJanuary() {
        LocalDate date = LocalDate.of(2025, 1, 31);
        assertEquals("2025-01-31", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    void testCalculateAverageWithPositiveValues() {
        assertEquals(5.0f, StatsFragment.calculateAverage(15.0f, 3), 0.001f);
    }

    @Test
    void testCalculateAverageWithZeroCount() {
        assertEquals(0.0f, StatsFragment.calculateAverage(100.0f, 0), 0.001f);
    }

    @Test
    void testCalculateAverageWithZeroTotal() {
        assertEquals(0.0f, StatsFragment.calculateAverage(0.0f, 5), 0.001f);
    }

    @Test
    void testCalculateAverageWithOneItem() {
        assertEquals(42.0f, StatsFragment.calculateAverage(42.0f, 1), 0.001f);
    }

    @Test
    void testCalculateAverageWithFractionalResult() {
        assertEquals(3.333f, StatsFragment.calculateAverage(10.0f, 3), 0.01f);
    }

    @Test
    void testGenerateDateLabelsForSingleDay() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        List<String> labels = StatsFragment.generateDateLabels(date, date);
        assertEquals(1, labels.size());
        assertEquals("2025-06-15", labels.get(0));
    }

    @Test
    void testGenerateDateLabelsForThreeDays() {
        LocalDate start = LocalDate.of(2025, 6, 13);
        LocalDate end = LocalDate.of(2025, 6, 15);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(3, labels.size());
        assertEquals("2025-06-13", labels.get(0));
        assertEquals("2025-06-14", labels.get(1));
        assertEquals("2025-06-15", labels.get(2));
    }

    @Test
    void testGenerateDateLabelsSpanningMonthBoundary() {
        LocalDate start = LocalDate.of(2025, 1, 30);
        LocalDate end = LocalDate.of(2025, 2, 2);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(4, labels.size());
        assertEquals("2025-01-30", labels.get(0));
        assertEquals("2025-01-31", labels.get(1));
        assertEquals("2025-02-01", labels.get(2));
        assertEquals("2025-02-02", labels.get(3));
    }

    @Test
    void testGenerateDateLabelsFor30Days() {
        LocalDate end = LocalDate.of(2025, 6, 30);
        LocalDate start = end.minusDays(StatsFragment.DAYS - 1);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(StatsFragment.DAYS, labels.size());
    }

    @Test
    void testGenerateDateLabelsStartAfterEndReturnsEmpty() {
        LocalDate start = LocalDate.of(2025, 6, 15);
        LocalDate end = LocalDate.of(2025, 6, 14);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertTrue(labels.isEmpty());
    }

    @Test
    void testCalculateAverageWithLargeTotal() {
        assertEquals(1000.0f, StatsFragment.calculateAverage(10000.0f, 10), 0.001f);
    }

    @Test
    void testCalculateAverageWithSmallFractionalValues() {
        assertEquals(0.1f, StatsFragment.calculateAverage(0.3f, 3), 0.01f);
    }

    @Test
    void testCalculateAverageWithNegativeTotal() {
        assertEquals(-5.0f, StatsFragment.calculateAverage(-10.0f, 2), 0.001f);
    }

    @Test
    void testCalculateAverageWithNegativeCount() {
        // Negative count returns 0 (treated same as zero count)
        float result = StatsFragment.calculateAverage(10.0f, -2);
        assertEquals(0.0f, result, 0.001f);
    }

    @Test
    void testGenerateDateLabelsSpanningYearBoundary() {
        LocalDate start = LocalDate.of(2024, 12, 30);
        LocalDate end = LocalDate.of(2025, 1, 2);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(4, labels.size());
        assertEquals("2024-12-30", labels.get(0));
        assertEquals("2024-12-31", labels.get(1));
        assertEquals("2025-01-01", labels.get(2));
        assertEquals("2025-01-02", labels.get(3));
    }

    @Test
    void testGenerateDateLabelsSpanningLeapDay() {
        LocalDate start = LocalDate.of(2024, 2, 28);
        LocalDate end = LocalDate.of(2024, 3, 1);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(3, labels.size());
        assertEquals("2024-02-28", labels.get(0));
        assertEquals("2024-02-29", labels.get(1));
        assertEquals("2024-03-01", labels.get(2));
    }

    @Test
    void testLabelPatternIsYyyyMMdd() {
        assertEquals("yyyy-MM-dd", StatsFragment.LABEL_PATTERN);
    }

    @Test
    void testLabelFormatWithFebruary() {
        LocalDate date = LocalDate.of(2025, 2, 14);
        assertEquals("2025-02-14", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    void testGenerateDateLabelsAllLabelsHaveDash() {
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end = LocalDate.of(2025, 6, 10);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        for (String label : labels) {
            assertTrue(label.contains("-"), "Each label should contain a dash separator");
        }
    }

    @Test
    void testGenerateDateLabelsAllLabelsHaveTenCharacters() {
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end = LocalDate.of(2025, 6, 10);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        for (String label : labels) {
            assertEquals(10, label.length(), "Each label should be in yyyy-MM-dd format (10 chars)");
        }
    }
}
