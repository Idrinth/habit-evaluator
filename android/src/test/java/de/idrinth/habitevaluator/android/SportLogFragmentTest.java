package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SportLogFragmentTest {

    @Test
    void testDatePatternValue() {
        assertEquals("yyyy-MM-dd", SportLogFragment.DATE_PATTERN);
    }

    @Test
    void testTimePatternValue() {
        assertEquals("HH:mm", SportLogFragment.TIME_PATTERN);
    }

    @Test
    void testDateFormatNotNull() {
        assertNotNull(SportLogFragment.DATE_FORMAT);
    }

    @Test
    void testTimeFormatNotNull() {
        assertNotNull(SportLogFragment.TIME_FORMAT);
    }

    @Test
    void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 7, 4);
        assertEquals("2025-07-04", SportLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testDateFormatWithSingleDigitMonth() {
        LocalDate date = LocalDate.of(2025, 1, 9);
        assertEquals("2025-01-09", SportLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(9, 45);
        assertEquals("09:45", SportLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatEndOfDay() {
        LocalTime time = LocalTime.of(23, 59);
        assertEquals("23:59", SportLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", SportLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testDatePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.DATE_PATTERN, SportLogFragment.DATE_PATTERN);
    }

    @Test
    void testTimePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.TIME_PATTERN, SportLogFragment.TIME_PATTERN);
    }
}
