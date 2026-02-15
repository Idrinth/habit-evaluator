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

    @Test
    void testTimeFormatRoundTrip() {
        LocalTime original = LocalTime.of(14, 30);
        String formatted = SleepTrackingFragment.TIME_FORMAT.format(original);
        LocalTime parsed = LocalTime.parse(formatted, SleepTrackingFragment.TIME_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    void testDateFormatRoundTrip() {
        LocalDate original = LocalDate.of(2025, 6, 15);
        String formatted = SleepTrackingFragment.DATE_FORMAT.format(original);
        LocalDate parsed = LocalDate.parse(formatted, SleepTrackingFragment.DATE_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    void testTimeFormatEndOfDay() {
        LocalTime time = LocalTime.of(23, 59);
        assertEquals("23:59", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatNoon() {
        LocalTime time = LocalTime.of(12, 0);
        assertEquals("12:00", SleepTrackingFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testDateFormatEndOfYear() {
        LocalDate date = LocalDate.of(2025, 12, 31);
        assertEquals("2025-12-31", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testDateFormatStartOfYear() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertEquals("2025-01-01", SleepTrackingFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testTimePatternMatchesSportLogPattern() {
        assertEquals(SportLogFragment.TIME_PATTERN, SleepTrackingFragment.TIME_PATTERN);
    }

    @Test
    void testDatePatternMatchesSportLogPattern() {
        assertEquals(SportLogFragment.DATE_PATTERN, SleepTrackingFragment.DATE_PATTERN);
    }
}
