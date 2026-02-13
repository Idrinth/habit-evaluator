package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SleepTrackingFragmentTest {

    @Test
    void testTimePatternValue() {
        assertEquals("HH:mm", SleepTrackingFragment.TIME_PATTERN);
    }

    @Test
    void testDatePatternValue() {
        assertEquals("yyyy-MM-dd", SleepTrackingFragment.DATE_PATTERN);
    }

    @Test
    void testTimeFormatNotNull() {
        assertNotNull(SleepTrackingFragment.TIME_FORMAT);
    }

    @Test
    void testDateFormatNotNull() {
        assertNotNull(SleepTrackingFragment.DATE_FORMAT);
    }

    @Test
    void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(14, 30);
        assertEquals("14:30", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatSingleDigitHour() {
        LocalTime time = LocalTime.of(7, 5);
        assertEquals("07:05", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        assertEquals("2025-03-15", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testDateFormatWithSingleDigitMonth() {
        LocalDate date = LocalDate.of(2025, 1, 5);
        assertEquals("2025-01-05", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testDateFormatLeapYearDate() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        assertEquals("2024-02-29", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testTimePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.TIME_PATTERN, SleepTrackingFragment.TIME_PATTERN);
    }

    @Test
    void testDatePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.DATE_PATTERN, SleepTrackingFragment.DATE_PATTERN);
    }
}
