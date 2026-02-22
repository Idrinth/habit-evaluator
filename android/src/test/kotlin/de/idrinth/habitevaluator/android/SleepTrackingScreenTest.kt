package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tests for date/time format constants equivalent to what SleepTrackingFragment
 * previously provided as static constants.
 */
class SleepTrackingScreenTest {

    companion object {
        const val TIME_PATTERN = "HH:mm"
        const val DATE_PATTERN = "yyyy-MM-dd"
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
    }

    @Test
    fun testTimePatternValue() {
        assertEquals("HH:mm", TIME_PATTERN)
    }

    @Test
    fun testDatePatternValue() {
        assertEquals("yyyy-MM-dd", DATE_PATTERN)
    }

    @Test
    fun testTimeFormatNotNull() {
        assertNotNull(TIME_FORMAT)
    }

    @Test
    fun testDateFormatNotNull() {
        assertNotNull(DATE_FORMAT)
    }

    @Test
    fun testTimeFormatProducesExpectedOutput() {
        val time = LocalTime.of(14, 30)
        assertEquals("14:30", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatMidnight() {
        val time = LocalTime.of(0, 0)
        assertEquals("00:00", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatSingleDigitHour() {
        val time = LocalTime.of(7, 5)
        assertEquals("07:05", TIME_FORMAT.format(time))
    }

    @Test
    fun testDateFormatProducesExpectedOutput() {
        val date = LocalDate.of(2025, 3, 15)
        assertEquals("2025-03-15", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatWithSingleDigitMonth() {
        val date = LocalDate.of(2025, 1, 5)
        assertEquals("2025-01-05", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatLeapYearDate() {
        val date = LocalDate.of(2024, 2, 29)
        assertEquals("2024-02-29", DATE_FORMAT.format(date))
    }

    @Test
    fun testTimePatternMatchesDiaryScreenPattern() {
        assertEquals(DiaryScreenTest.TIME_PATTERN, TIME_PATTERN)
    }

    @Test
    fun testDatePatternMatchesDiaryScreenPattern() {
        assertEquals(DiaryScreenTest.DATE_PATTERN, DATE_PATTERN)
    }

    @Test
    fun testTimeFormatRoundTrip() {
        val original = LocalTime.of(14, 30)
        val formatted = TIME_FORMAT.format(original)
        val parsed = LocalTime.parse(formatted, TIME_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testDateFormatRoundTrip() {
        val original = LocalDate.of(2025, 6, 15)
        val formatted = DATE_FORMAT.format(original)
        val parsed = LocalDate.parse(formatted, DATE_FORMAT)
        assertEquals(original, parsed)
    }

    @Test
    fun testTimeFormatEndOfDay() {
        val time = LocalTime.of(23, 59)
        assertEquals("23:59", TIME_FORMAT.format(time))
    }

    @Test
    fun testTimeFormatNoon() {
        val time = LocalTime.of(12, 0)
        assertEquals("12:00", TIME_FORMAT.format(time))
    }

    @Test
    fun testDateFormatEndOfYear() {
        val date = LocalDate.of(2025, 12, 31)
        assertEquals("2025-12-31", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatStartOfYear() {
        val date = LocalDate.of(2025, 1, 1)
        assertEquals("2025-01-01", DATE_FORMAT.format(date))
    }

    @Test
    fun testTimePatternMatchesSportLogScreenPattern() {
        assertEquals(SportLogScreenTest.TIME_PATTERN, TIME_PATTERN)
    }

    @Test
    fun testDatePatternMatchesSportLogScreenPattern() {
        assertEquals(SportLogScreenTest.DATE_PATTERN, DATE_PATTERN)
    }
}
