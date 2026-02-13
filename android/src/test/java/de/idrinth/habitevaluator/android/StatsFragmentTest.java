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
        assertEquals("03/15", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    void testLabelFormatWithDecember() {
        LocalDate date = LocalDate.of(2025, 12, 1);
        assertEquals("12/01", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    void testLabelFormatWithJanuary() {
        LocalDate date = LocalDate.of(2025, 1, 31);
        assertEquals("01/31", StatsFragment.LABEL_FORMAT.format(date));
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
        assertEquals("06/15", labels.get(0));
    }

    @Test
    void testGenerateDateLabelsForThreeDays() {
        LocalDate start = LocalDate.of(2025, 6, 13);
        LocalDate end = LocalDate.of(2025, 6, 15);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(3, labels.size());
        assertEquals("06/13", labels.get(0));
        assertEquals("06/14", labels.get(1));
        assertEquals("06/15", labels.get(2));
    }

    @Test
    void testGenerateDateLabelsSpanningMonthBoundary() {
        LocalDate start = LocalDate.of(2025, 1, 30);
        LocalDate end = LocalDate.of(2025, 2, 2);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(4, labels.size());
        assertEquals("01/30", labels.get(0));
        assertEquals("01/31", labels.get(1));
        assertEquals("02/01", labels.get(2));
        assertEquals("02/02", labels.get(3));
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
}
