package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class SleepTrackingFragmentTest {

    @Test
    public void testTimePatternValue() {
        assertEquals("HH:mm", SleepTrackingFragment.TIME_PATTERN);
    }

    @Test
    public void testDatePatternValue() {
        assertEquals("yyyy-MM-dd", SleepTrackingFragment.DATE_PATTERN);
    }

    @Test
    public void testTimeFormatNotNull() {
        assertNotNull(SleepTrackingFragment.TIME_FORMAT);
    }

    @Test
    public void testDateFormatNotNull() {
        assertNotNull(SleepTrackingFragment.DATE_FORMAT);
    }

    @Test
    public void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(14, 30);
        assertEquals("14:30", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatSingleDigitHour() {
        LocalTime time = LocalTime.of(7, 5);
        assertEquals("07:05", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        assertEquals("2025-03-15", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testDateFormatWithSingleDigitMonth() {
        LocalDate date = LocalDate.of(2025, 1, 5);
        assertEquals("2025-01-05", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testDateFormatLeapYearDate() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        assertEquals("2024-02-29", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testTimePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.TIME_PATTERN, SleepTrackingFragment.TIME_PATTERN);
    }

    @Test
    public void testDatePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.DATE_PATTERN, SleepTrackingFragment.DATE_PATTERN);
    }
}
