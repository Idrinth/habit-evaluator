package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MedicationLogFragmentTest {

    @Test
    void testDtDisplayPatternValue() {
        assertEquals("yyyy-MM-dd HH:mm", MedicationLogFragment.DT_DISPLAY_PATTERN);
    }

    @Test
    void testDtDisplayFormatNotNull() {
        assertNotNull(MedicationLogFragment.DT_DISPLAY_FORMAT);
    }

    @Test
    void testDtDisplayFormatProducesExpectedOutput() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 7, 4, 12, 30);
        assertEquals("2025-07-04 12:30", MedicationLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayFormatMidnight() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        assertEquals("2025-01-01 00:00", MedicationLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayFormatEndOfYear() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 31, 23, 59);
        assertEquals("2025-12-31 23:59", MedicationLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayFormatSingleDigitValues() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 5, 8, 7);
        assertEquals("2025-03-05 08:07", MedicationLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayPatternMatchesFoodLogFragmentPattern() {
        assertEquals(FoodLogFragment.DT_DISPLAY_PATTERN, MedicationLogFragment.DT_DISPLAY_PATTERN);
    }

    @Test
    void testDtDisplayFormatRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2025, 9, 22, 14, 45);
        String formatted = MedicationLogFragment.DT_DISPLAY_FORMAT.format(original);
        LocalDateTime parsed = LocalDateTime.parse(formatted, MedicationLogFragment.DT_DISPLAY_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    void testDtDisplayFormatNoon() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 15, 12, 0);
        assertEquals("2025-06-15 12:00", MedicationLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayFormatLeapYearDate() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 29, 8, 30);
        assertEquals("2024-02-29 08:30", MedicationLogFragment.DT_DISPLAY_FORMAT.format(dateTime));
    }

    @Test
    void testDtDisplayPatternContainsDateAndTimeParts() {
        assertTrue(MedicationLogFragment.DT_DISPLAY_PATTERN.contains("yyyy"));
        assertTrue(MedicationLogFragment.DT_DISPLAY_PATTERN.contains("MM"));
        assertTrue(MedicationLogFragment.DT_DISPLAY_PATTERN.contains("dd"));
        assertTrue(MedicationLogFragment.DT_DISPLAY_PATTERN.contains("HH"));
        assertTrue(MedicationLogFragment.DT_DISPLAY_PATTERN.contains("mm"));
    }
}
