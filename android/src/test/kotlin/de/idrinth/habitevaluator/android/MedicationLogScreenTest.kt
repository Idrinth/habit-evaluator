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

        /**
         * Replication of dose parsing logic from MedicationLogScreen.
         * Returns the parsed dose as a Double, or null if invalid.
         */
        fun parseDose(input: String): Double? {
            return input.toDoubleOrNull()
        }

        /**
         * Replication of dose display formatting from MedicationLogScreen.
         * Formats the dose amount with the medication unit string.
         */
        fun formatDoseDisplay(amount: Double, unit: String?): String {
            return "$amount ${unit ?: ""}"
        }
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

    // --- Dose parsing tests ---

    @Test
    fun testParseDoseValidInteger() {
        assertEquals(2.0, parseDose("2")!!, 0.001)
    }

    @Test
    fun testParseDoseValidDecimal() {
        assertEquals(1.5, parseDose("1.5")!!, 0.001)
    }

    @Test
    fun testParseDoseInvalidReturnsNull() {
        assertNull(parseDose("abc"))
    }

    @Test
    fun testParseDoseEmptyReturnsNull() {
        assertNull(parseDose(""))
    }

    @Test
    fun testParseDoseZero() {
        assertEquals(0.0, parseDose("0")!!, 0.001)
    }

    @Test
    fun testParseDoseNegative() {
        assertEquals(-1.0, parseDose("-1")!!, 0.001)
    }

    @Test
    fun testParseDoseSmallDecimal() {
        assertEquals(0.25, parseDose("0.25")!!, 0.001)
    }

    // --- Dose display formatting tests ---

    @Test
    fun testFormatDoseDisplayWithUnit() {
        assertEquals("2.0 pills", formatDoseDisplay(2.0, "pills"))
    }

    @Test
    fun testFormatDoseDisplayNullUnit() {
        assertEquals("1.5 ", formatDoseDisplay(1.5, null))
    }

    @Test
    fun testFormatDoseDisplayEmptyUnit() {
        assertEquals("3.0 ", formatDoseDisplay(3.0, ""))
    }

    @Test
    fun testFormatDoseDisplayZeroDose() {
        assertEquals("0.0 ml", formatDoseDisplay(0.0, "ml"))
    }
}
