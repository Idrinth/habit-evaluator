package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class FoodLogFragmentTest {

    @Test
    public void testDatePatternValue() {
        assertEquals("yyyy-MM-dd", FoodLogFragment.DATE_PATTERN);
    }

    @Test
    public void testTimePatternValue() {
        assertEquals("HH:mm", FoodLogFragment.TIME_PATTERN);
    }

    @Test
    public void testDtDisplayPatternValue() {
        assertEquals("yyyy-MM-dd HH:mm", FoodLogFragment.DT_DISPLAY_PATTERN);
    }

    @Test
    public void testDateFormatNotNull() {
        assertNotNull(FoodLogFragment.DATE_FORMAT);
    }

    @Test
    public void testTimeFormatNotNull() {
        assertNotNull(FoodLogFragment.TIME_FORMAT);
    }

    @Test
    public void testDtDisplayFormatNotNull() {
        assertNotNull(FoodLogFragment.DT_DISPLAY_FORMAT);
    }

    @Test
    public void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 7, 4);
        assertEquals("2025-07-04", FoodLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testDateFormatLeapYearDate() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        assertEquals("2024-02-29", FoodLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(8, 30);
        assertEquals("08:30", FoodLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatEndOfDay() {
        LocalTime time = LocalTime.of(23, 59);
        assertEquals("23:59", FoodLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testDtDisplayFormatProducesExpectedOutput() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 7, 4, 12, 30);
        assertEquals("2025-07-04 12:30", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    public void testDtDisplayFormatMidnight() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        assertEquals("2025-01-01 00:00", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    public void testDtDisplayFormatEndOfYear() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 31, 23, 59);
        assertEquals("2025-12-31 23:59", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    public void testDatePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.DATE_PATTERN, FoodLogFragment.DATE_PATTERN);
    }

    @Test
    public void testTimePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.TIME_PATTERN, FoodLogFragment.TIME_PATTERN);
    }
}
