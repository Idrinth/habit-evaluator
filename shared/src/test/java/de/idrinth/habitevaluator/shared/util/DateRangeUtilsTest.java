package de.idrinth.habitevaluator.shared.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeUtilsTest {

    @Test
    void testGetDateRangeSingleDay() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<LocalDate> range = DateRangeUtils.getDateRange(date, date);
        assertEquals(1, range.size());
        assertEquals(date, range.get(0));
    }

    @Test
    void testGetDateRangeMultipleDays() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 5);
        List<LocalDate> range = DateRangeUtils.getDateRange(from, to);
        assertEquals(5, range.size());
        assertEquals(from, range.get(0));
        assertEquals(to, range.get(4));
    }

    @Test
    void testGetDateRangeFromAfterToReturnsEmpty() {
        LocalDate from = LocalDate.of(2024, 1, 10);
        LocalDate to = LocalDate.of(2024, 1, 5);
        List<LocalDate> range = DateRangeUtils.getDateRange(from, to);
        assertTrue(range.isEmpty());
    }

    @Test
    void testFormatDateLabels() {
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 3, 3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        List<String> labels = DateRangeUtils.formatDateLabels(from, to, formatter);
        assertEquals(3, labels.size());
        assertEquals("03/01", labels.get(0));
        assertEquals("03/02", labels.get(1));
        assertEquals("03/03", labels.get(2));
    }

    @Test
    void testFormatDateLabelsEmptyRange() {
        LocalDate from = LocalDate.of(2024, 3, 5);
        LocalDate to = LocalDate.of(2024, 3, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        List<String> labels = DateRangeUtils.formatDateLabels(from, to, formatter);
        assertTrue(labels.isEmpty());
    }

    @Test
    void testFilterByDateRange() {
        LocalDate from = LocalDate.of(2024, 1, 5);
        LocalDate to = LocalDate.of(2024, 1, 10);
        List<LocalDate> items = Arrays.asList(
                LocalDate.of(2024, 1, 3),
                LocalDate.of(2024, 1, 5),
                LocalDate.of(2024, 1, 7),
                LocalDate.of(2024, 1, 10),
                LocalDate.of(2024, 1, 12)
        );
        List<LocalDate> filtered = DateRangeUtils.filterByDateRange(items, d -> d, from, to);
        assertEquals(3, filtered.size());
        assertEquals(LocalDate.of(2024, 1, 5), filtered.get(0));
        assertEquals(LocalDate.of(2024, 1, 7), filtered.get(1));
        assertEquals(LocalDate.of(2024, 1, 10), filtered.get(2));
    }

    @Test
    void testFilterByDateRangeWithNullDate() {
        LocalDate from = LocalDate.of(2024, 1, 5);
        LocalDate to = LocalDate.of(2024, 1, 10);
        List<String> items = Arrays.asList("a", "b");
        List<String> filtered = DateRangeUtils.filterByDateRange(items, s -> null, from, to);
        assertTrue(filtered.isEmpty());
    }

    @Test
    void testIsInRange() {
        LocalDate from = LocalDate.of(2024, 1, 5);
        LocalDate to = LocalDate.of(2024, 1, 10);
        assertTrue(DateRangeUtils.isInRange(LocalDate.of(2024, 1, 5), from, to));
        assertTrue(DateRangeUtils.isInRange(LocalDate.of(2024, 1, 7), from, to));
        assertTrue(DateRangeUtils.isInRange(LocalDate.of(2024, 1, 10), from, to));
        assertFalse(DateRangeUtils.isInRange(LocalDate.of(2024, 1, 4), from, to));
        assertFalse(DateRangeUtils.isInRange(LocalDate.of(2024, 1, 11), from, to));
    }

    @Test
    void testIsInRangeWithNull() {
        LocalDate from = LocalDate.of(2024, 1, 5);
        LocalDate to = LocalDate.of(2024, 1, 10);
        assertFalse(DateRangeUtils.isInRange(null, from, to));
    }

    @Test
    void testDaysBetweenInclusive() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 10);
        assertEquals(10, DateRangeUtils.daysBetweenInclusive(from, to));
    }

    @Test
    void testDaysBetweenInclusiveSameDay() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        assertEquals(1, DateRangeUtils.daysBetweenInclusive(date, date));
    }

    @Test
    void testGetDayIndex() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        assertEquals(0, DateRangeUtils.getDayIndex(LocalDate.of(2024, 1, 1), start));
        assertEquals(4, DateRangeUtils.getDayIndex(LocalDate.of(2024, 1, 5), start));
        assertEquals(30, DateRangeUtils.getDayIndex(LocalDate.of(2024, 1, 31), start));
    }

    @Test
    void testGetDayIndexBeforeStartReturnsNegativeOne() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        assertEquals(-1, DateRangeUtils.getDayIndex(LocalDate.of(2024, 1, 5), start));
    }
}
