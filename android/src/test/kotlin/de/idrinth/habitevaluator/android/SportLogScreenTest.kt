package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tests for date/time format constants equivalent to what SportLogFragment previously provided.
 */
class SportLogScreenTest {

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm"
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)
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
    fun testDateFormatNotNull() {
        assertNotNull(DATE_FORMAT)
    }

    @Test
    fun testTimeFormatNotNull() {
        assertNotNull(TIME_FORMAT)
    }

    @Test
    fun testDateFormatProducesExpectedOutput() {
        val date = LocalDate.of(2025, 7, 4)
        assertEquals("2025-07-04", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatWithSingleDigitMonth() {
        val date = LocalDate.of(2025, 1, 9)
        assertEquals("2025-01-09", DATE_FORMAT.format(date))
    }

    @Test
    fun testTimeFormatProducesExpectedOutput() {
        val time = LocalTime.of(9, 45)
        assertEquals("09:45", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatEndOfDay() {
        val time = LocalTime.of(23, 59)
        assertEquals("23:59", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatMidnight() {
        val time = LocalTime.of(0, 0)
        assertEquals("00:00", TIME_FORMAT.format(time))
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
        val original = LocalDate.of(2025, 8, 20)
        val formatted = DATE_FORMAT.format(original)
        val parsed = LocalDate.parse(formatted, DATE_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testTimeFormatRoundTrip() {
        val original = LocalTime.of(16, 45)
        val formatted = TIME_FORMAT.format(original)
        val parsed = LocalTime.parse(formatted, TIME_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testDateFormatLeapYearDate() {
        val date = LocalDate.of(2024, 2, 29)
        assertEquals("2024-02-29", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatEndOfYear() {
        val date = LocalDate.of(2025, 12, 31)
        assertEquals("2025-12-31", DATE_FORMAT.format(date))
    }

    @Test
    fun testTimeFormatNoon() {
        val time = LocalTime.of(12, 0)
        assertEquals("12:00", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatSingleDigitHour() {
        val time = LocalTime.of(5, 3)
        assertEquals("05:03", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimePatternMatchesSleepTrackingScreenPattern() {
        assertEquals(SleepTrackingScreenTest.TIME_PATTERN, TIME_PATTERN)
    }

    @Test
    fun testDatePatternMatchesSleepTrackingScreenPattern() {
        assertEquals(SleepTrackingScreenTest.DATE_PATTERN, DATE_PATTERN)
    }
}
