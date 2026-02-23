package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tests for date/time format constants equivalent to what FoodLogFragment previously provided.
 */
class FoodLogScreenTest {

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm"
        const val DT_DISPLAY_PATTERN = "yyyy-MM-dd HH:mm"
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)
        val DT_DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DT_DISPLAY_PATTERN)

        /**
         * Replication of food tag splitting logic from FoodLogScreen.
         * Splits a comma-separated string into trimmed, non-empty tags.
         */
        fun splitFoodTags(items: String): List<String> {
            return items.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        /**
         * Replication of kcal parsing logic from FoodLogScreen.
         * Returns the parsed integer if non-blank and valid, null if blank, null if invalid.
         */
        fun parseKcal(input: String): Int? {
            if (input.isBlank()) return null
            return input.toIntOrNull()
        }

        /**
         * Replication of carbs parsing logic from FoodLogScreen.
         * Returns the parsed double if non-blank and valid, null if blank, null if invalid.
         */
        fun parseCarbs(input: String): Double? {
            if (input.isBlank()) return null
            return input.toDoubleOrNull()
        }
    }

    @Test
    fun testDatePatternValue() {
        assertEquals("yyyy-MM-dd", DATE_PATTERN)
    }

    @Test
    fun testTimePatternValue() {
        assertEquals("HH:mm", TIME_PATTERN)
    }

    @Test
    fun testDtDisplayPatternValue() {
        assertEquals("yyyy-MM-dd HH:mm", DT_DISPLAY_PATTERN)
    }

    @Test
    fun testDateFormatNotNull() {
        assertNotNull(DATE_FORMAT)
    }

    @Test
    fun testTimeFormatNotNull() {
        assertNotNull(TIME_FORMAT)
    }

    @Test
    fun testDtDisplayFormatNotNull() {
        assertNotNull(DT_DISPLAY_FORMAT)
    }

    @Test
    fun testDateFormatProducesExpectedOutput() {
        val date = LocalDate.of(2025, 7, 4)
        assertEquals("2025-07-04", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatLeapYearDate() {
        val date = LocalDate.of(2024, 2, 29)
        assertEquals("2024-02-29", DATE_FORMAT.format(date))
    }

    @Test
    fun testTimeFormatProducesExpectedOutput() {
        val time = LocalTime.of(8, 30)
        assertEquals("08:30", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatEndOfDay() {
        val time = LocalTime.of(23, 59)
        assertEquals("23:59", TIME_FORMAT.format(time))
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
    fun testDatePatternMatchesDiaryScreenPattern() {
        assertEquals(DiaryScreenTest.DATE_PATTERN, DATE_PATTERN)
    }

    @Test
    fun testTimePatternMatchesDiaryScreenPattern() {
        assertEquals(DiaryScreenTest.TIME_PATTERN, TIME_PATTERN)
    }

    @Test
    fun testDateFormatRoundTrip() {
        val original = LocalDate.of(2025, 11, 3)
        val formatted = DATE_FORMAT.format(original)
        val parsed = LocalDate.parse(formatted, DATE_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testTimeFormatRoundTrip() {
        val original = LocalTime.of(19, 15)
        val formatted = TIME_FORMAT.format(original)
        val parsed = LocalTime.parse(formatted, TIME_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testDtDisplayFormatRoundTrip() {
        val original = LocalDateTime.of(2025, 4, 10, 7, 30)
        val formatted = DT_DISPLAY_FORMAT.format(original)
        val parsed = LocalDateTime.parse(formatted, DT_DISPLAY_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testDtDisplayFormatWithNoon() {
        val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0)
        assertEquals("2025-06-15 12:00", DT_DISPLAY_FORMAT.format(dateTime))
    }

    @Test
    fun testDtDisplayPatternContainsDateAndTimeParts() {
        assertTrue(DT_DISPLAY_PATTERN.contains("yyyy"))
        assertTrue(DT_DISPLAY_PATTERN.contains("HH"))
        assertTrue(DT_DISPLAY_PATTERN.contains("mm"))
    }

    @Test
    fun testDtDisplayPatternMatchesMedicationLogScreenPattern() {
        assertEquals(MedicationLogScreenTest.DT_DISPLAY_PATTERN, DT_DISPLAY_PATTERN)
    }

    @Test
    fun testDateFormatStartOfYear() {
        val date = LocalDate.of(2025, 1, 1)
        assertEquals("2025-01-01", DATE_FORMAT.format(date))
    }

    @Test
    fun testTimeFormatMidnight() {
        val time = LocalTime.of(0, 0)
        assertEquals("00:00", TIME_FORMAT.format(time))
    }

    // --- Tag splitting logic tests ---

    @Test
    fun testSplitFoodTagsSingleItem() {
        val result = splitFoodTags("apple")
        assertEquals(listOf("apple"), result)
    }

    @Test
    fun testSplitFoodTagsMultipleItems() {
        val result = splitFoodTags("apple, banana, cherry")
        assertEquals(listOf("apple", "banana", "cherry"), result)
    }

    @Test
    fun testSplitFoodTagsTrimsWhitespace() {
        val result = splitFoodTags("  apple ,  banana  , cherry  ")
        assertEquals(listOf("apple", "banana", "cherry"), result)
    }

    @Test
    fun testSplitFoodTagsFiltersEmpty() {
        val result = splitFoodTags("apple,,banana,,,cherry")
        assertEquals(listOf("apple", "banana", "cherry"), result)
    }

    @Test
    fun testSplitFoodTagsEmptyString() {
        val result = splitFoodTags("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSplitFoodTagsOnlyCommas() {
        val result = splitFoodTags(",,,")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSplitFoodTagsWhitespaceOnly() {
        val result = splitFoodTags("   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSplitFoodTagsCommasAndSpacesOnly() {
        val result = splitFoodTags(" , , , ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSplitFoodTagsSingleItemWithTrailingComma() {
        val result = splitFoodTags("apple,")
        assertEquals(listOf("apple"), result)
    }

    // --- Kcal validation tests ---

    @Test
    fun testParseKcalValidInteger() {
        assertEquals(250, parseKcal("250"))
    }

    @Test
    fun testParseKcalEmptyReturnsNull() {
        assertNull(parseKcal(""))
    }

    @Test
    fun testParseKcalBlankReturnsNull() {
        assertNull(parseKcal("   "))
    }

    @Test
    fun testParseKcalInvalidReturnsNull() {
        assertNull(parseKcal("abc"))
    }

    @Test
    fun testParseKcalZero() {
        assertEquals(0, parseKcal("0"))
    }

    @Test
    fun testParseKcalDecimalReturnsNull() {
        assertNull(parseKcal("250.5"))
    }

    // --- Carbs validation tests ---

    @Test
    fun testParseCarbsValidDouble() {
        assertEquals(30.5, parseCarbs("30.5")!!, 0.001)
    }

    @Test
    fun testParseCarbsEmptyReturnsNull() {
        assertNull(parseCarbs(""))
    }

    @Test
    fun testParseCarbsBlankReturnsNull() {
        assertNull(parseCarbs("   "))
    }

    @Test
    fun testParseCarbsInvalidReturnsNull() {
        assertNull(parseCarbs("abc"))
    }

    @Test
    fun testParseCarbsInteger() {
        assertEquals(30.0, parseCarbs("30")!!, 0.001)
    }

    @Test
    fun testParseCarbsZero() {
        assertEquals(0.0, parseCarbs("0")!!, 0.001)
    }
}
