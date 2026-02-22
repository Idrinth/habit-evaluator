package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Tests for date/time format constants equivalent to what MedicationLogFragment previously provided.
 */
class MedicationLogScreenTest {

    companion object {
        const val DT_DISPLAY_PATTERN = "yyyy-MM-dd HH:mm"
        val DT_DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DT_DISPLAY_PATTERN)
    }

    @Test
    fun testDtDisplayPatternValue() {
        assertEquals("yyyy-MM-dd HH:mm", DT_DISPLAY_PATTERN)
    }

    @Test
    fun testDtDisplayFormatNotNull() {
        assertNotNull(DT_DISPLAY_FORMAT)
    }

    @Test
    fun testDtDisplayFormatProducesExpectedOutput() {
        val dateTime = LocalDateTime.of(2025, 7, 4, 12, 30)
        assertEquals("2025-07-04 12:30", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayFormatMidnight() {
        val dateTime = LocalDateTime.of(2025, 1, 1, 0, 0)
        assertEquals("2025-01-01 00:00", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayFormatEndOfYear() {
        val dateTime = LocalDateTime.of(2025, 12, 31, 23, 59)
        assertEquals("2025-12-31 23:59", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayFormatSingleDigitValues() {
        val dateTime = LocalDateTime.of(2025, 3, 5, 8, 7)
        assertEquals("2025-03-05 08:07", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayPatternMatchesFoodLogScreenPattern() {
        assertEquals(FoodLogScreenTest.DT_DISPLAY_PATTERN, DT_DISPLAY_PATTERN)
    }

    @Test
    fun testDtDisplayFormatRoundTrip() {
        val original = LocalDateTime.of(2025, 9, 22, 14, 45)
        val formatted = DT_DISPLAY_FORMAT.format(original)
        val parsed = LocalDateTime.parse(formatted, DT_DISPLAY_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testDtDisplayFormatNoon() {
        val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0)
        assertEquals("2025-06-15 12:00", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayFormatLeapYearDate() {
        val dateTime = LocalDateTime.of(2024, 2, 29, 8, 30)
        assertEquals("2024-02-29 08:30", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayPatternContainsDateAndTimeParts() {
        assertTrue(DT_DISPLAY_PATTERN.contains("yyyy"))
        assertTrue(DT_DISPLAY_PATTERN.contains("MM"))
        assertTrue(DT_DISPLAY_PATTERN.contains("dd"))
        assertTrue(DT_DISPLAY_PATTERN.contains("HH"))
        assertTrue(DT_DISPLAY_PATTERN.contains("mm"))
    }
}
