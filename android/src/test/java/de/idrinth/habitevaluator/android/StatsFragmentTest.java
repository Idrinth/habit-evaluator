package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class StatsFragmentTest {

    @Test
    public void testDaysConstant() {
        assertEquals(30, StatsFragment.DAYS);
    }

    @Test
    public void testLabelPatternNotNull() {
        assertNotNull(StatsFragment.LABEL_PATTERN);
    }

    @Test
    public void testLabelFormatNotNull() {
        assertNotNull(StatsFragment.LABEL_FORMAT);
    }

    @Test
    public void testLabelFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        assertEquals("03/15", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    public void testLabelFormatWithDecember() {
        LocalDate date = LocalDate.of(2025, 12, 1);
        assertEquals("12/01", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    public void testLabelFormatWithJanuary() {
        LocalDate date = LocalDate.of(2025, 1, 31);
        assertEquals("01/31", StatsFragment.LABEL_FORMAT.format(date));
    }

    @Test
    public void testCalculateAverageWithPositiveValues() {
        assertEquals(5.0f, StatsFragment.calculateAverage(15.0f, 3), 0.001f);
    }

    @Test
    public void testCalculateAverageWithZeroCount() {
        assertEquals(0.0f, StatsFragment.calculateAverage(100.0f, 0), 0.001f);
    }

    @Test
    public void testCalculateAverageWithZeroTotal() {
        assertEquals(0.0f, StatsFragment.calculateAverage(0.0f, 5), 0.001f);
    }

    @Test
    public void testCalculateAverageWithOneItem() {
        assertEquals(42.0f, StatsFragment.calculateAverage(42.0f, 1), 0.001f);
    }

    @Test
    public void testCalculateAverageWithFractionalResult() {
        assertEquals(3.333f, StatsFragment.calculateAverage(10.0f, 3), 0.01f);
    }

    @Test
    public void testGenerateDateLabelsForSingleDay() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        List<String> labels = StatsFragment.generateDateLabels(date, date);
        assertEquals(1, labels.size());
        assertEquals("06/15", labels.get(0));
    }

    @Test
    public void testGenerateDateLabelsForThreeDays() {
        LocalDate start = LocalDate.of(2025, 6, 13);
        LocalDate end = LocalDate.of(2025, 6, 15);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(3, labels.size());
        assertEquals("06/13", labels.get(0));
        assertEquals("06/14", labels.get(1));
        assertEquals("06/15", labels.get(2));
    }

    @Test
    public void testGenerateDateLabelsSpanningMonthBoundary() {
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
    public void testGenerateDateLabelsFor30Days() {
        LocalDate end = LocalDate.of(2025, 6, 30);
        LocalDate start = end.minusDays(StatsFragment.DAYS - 1);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertEquals(StatsFragment.DAYS, labels.size());
    }

    @Test
    public void testGenerateDateLabelsStartAfterEndReturnsEmpty() {
        LocalDate start = LocalDate.of(2025, 6, 15);
        LocalDate end = LocalDate.of(2025, 6, 14);
        List<String> labels = StatsFragment.generateDateLabels(start, end);
        assertTrue(labels.isEmpty());
    }
}
