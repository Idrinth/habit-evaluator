package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FoodLogFragmentTest {

    @Test
    void testDatePatternValue() {
        assertEquals("yyyy-MM-dd", FoodLogFragment.DATE_PATTERN);
    }

    @Test
    void testTimePatternValue() {
        assertEquals("HH:mm", FoodLogFragment.TIME_PATTERN);
    }

    @Test
    void testDtDisplayPatternValue() {
        assertEquals("yyyy-MM-dd HH:mm", FoodLogFragment.DT_DISPLAY_PATTERN);
    }

    @Test
    void testDateFormatNotNull() {
        assertNotNull(FoodLogFragment.DATE_FORMAT);
    }

    @Test
    void testTimeFormatNotNull() {
        assertNotNull(FoodLogFragment.TIME_FORMAT);
    }

    @Test
    void testDtDisplayFormatNotNull() {
        assertNotNull(FoodLogFragment.DT_DISPLAY_FORMAT);
    }

    @Test
    void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 7, 4);
        assertEquals("2025-07-04", FoodLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testDateFormatLeapYearDate() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        assertEquals("2024-02-29", FoodLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(8, 30);
        assertEquals("08:30", FoodLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatEndOfDay() {
        LocalTime time = LocalTime.of(23, 59);
        assertEquals("23:59", FoodLogFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testDtDisplayFormatProducesExpectedOutput() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 7, 4, 12, 30);
        assertEquals("2025-07-04 12:30", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayFormatMidnight() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        assertEquals("2025-01-01 00:00", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayFormatEndOfYear() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 31, 23, 59);
        assertEquals("2025-12-31 23:59", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDatePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.DATE_PATTERN, FoodLogFragment.DATE_PATTERN);
    }

    @Test
    void testTimePatternMatchesDiaryFragmentPattern() {
        assertEquals(DiaryFragment.TIME_PATTERN, FoodLogFragment.TIME_PATTERN);
    }

    @Test
    void testDateFormatRoundTrip() {
        LocalDate original = LocalDate.of(2025, 11, 3);
        String formatted = FoodLogFragment.DATE_FORMAT.format(original);
        LocalDate parsed = LocalDate.parse(formatted, FoodLogFragment.DATE_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    void testTimeFormatRoundTrip() {
        LocalTime original = LocalTime.of(19, 15);
        String formatted = FoodLogFragment.TIME_FORMAT.format(original);
        LocalTime parsed = LocalTime.parse(formatted, FoodLogFragment.TIME_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    void testDtDisplayFormatRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2025, 4, 10, 7, 30);
        String formatted = FoodLogFragment.DT_DISPLAY_FORMAT.format(original);
        LocalDateTime parsed = LocalDateTime.parse(formatted, FoodLogFragment.DT_DISPLAY_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    void testDtDisplayFormatWithNoon() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 15, 12, 0);
        assertEquals("2025-06-15 12:00", FoodLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayPatternContainsDateAndTimeParts() {
        assertTrue(FoodLogFragment.DT_DISPLAY_PATTERN.contains("yyyy"));
        assertTrue(FoodLogFragment.DT_DISPLAY_PATTERN.contains("HH"));
        assertTrue(FoodLogFragment.DT_DISPLAY_PATTERN.contains("mm"));
    }

    @Test
    void testDtDisplayPatternMatchesMedicationLogPattern() {
        assertEquals(MedicationLogFragment.DT_DISPLAY_PATTERN, FoodLogFragment.DT_DISPLAY_PATTERN);
    }

    @Test
    void testDateFormatStartOfYear() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertEquals("2025-01-01", FoodLogFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", FoodLogFragment.TIME_FORMAT.format(time));
    }
}
