package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class SportLogFragmentTest {

    @Test
    public void testDatePatternValue() {
        assertEquals("yyyy-MM-dd", SportLogFragment.DATE_PATTERN);
    }

    @Test
    public void testTimePatternValue() {
        assertEquals("HH:mm", SportLogFragment.TIME_PATTERN);
    }

    @Test
    public void testDateFormatNotNull() {
        assertNotNull(SportLogFragment.DATE_FORMAT);
    }

    @Test
    public void testTimeFormatNotNull() {
        assertNotNull(SportLogFragment.TIME_FORMAT);
    }

    @Test
    public void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 7, 4);
        assertEquals("2025-07-04", SportLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testDateFormatWithSingleDigitMonth() {
        LocalDate date = LocalDate.of(2025, 1, 9);
        assertEquals("2025-01-09", SportLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(9, 45);
        assertEquals("09:45", SportLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatEndOfDay() {
        LocalTime time = LocalTime.of(23, 59);
        assertEquals("23:59", SportLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", SportLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testDatePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.DATE_PATTERN, SportLogFragment.DATE_PATTERN);
    }

    @Test
    public void testTimePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.TIME_PATTERN, SportLogFragment.TIME_PATTERN);
    }
}
