package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tests for date/time format constants equivalent to what ActivityLogFragment previously provided.
 */
class ActivityLogScreenTest {

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm"
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)

        /**
         * Replication of form validation logic from ActivityLogScreen.
         * Returns an error key if validation fails, null if all fields are valid.
         */
        fun validateActivityForm(
            persons: String,
            location: String,
            startTime: LocalTime?,
            endTime: LocalTime?
        ): String? {
            if (persons.isBlank()) return "persons_required"
            if (location.isBlank()) return "location_required"
            if (startTime == null || endTime == null) return "times_required"
            return null
        }

        /**
         * Replication of time range display formatting from ActivityLogScreen.
         * Formats a start-end time range as "HH:mm - HH:mm" or partial if one is null.
         */
        fun formatTimeRange(startTime: LocalTime?, endTime: LocalTime?): String {
            val st = startTime?.format(TIME_FORMAT) ?: ""
            val et = endTime?.format(TIME_FORMAT) ?: ""
            return "$st - $et"
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

    // --- Form validation tests ---

    @Test
    fun testValidateActivityFormAllValid() {
        assertNull(validateActivityForm("John", "Office", LocalTime.of(9, 0), LocalTime.of(17, 0)))
    }

    @Test
    fun testValidateActivityFormBlankPersons() {
        assertEquals("persons_required", validateActivityForm("", "Office", LocalTime.of(9, 0), LocalTime.of(17, 0)))
    }

    @Test
    fun testValidateActivityFormWhitespacePersons() {
        assertEquals("persons_required", validateActivityForm("   ", "Office", LocalTime.of(9, 0), LocalTime.of(17, 0)))
    }

    @Test
    fun testValidateActivityFormBlankLocation() {
        assertEquals("location_required", validateActivityForm("John", "", LocalTime.of(9, 0), LocalTime.of(17, 0)))
    }

    @Test
    fun testValidateActivityFormWhitespaceLocation() {
        assertEquals("location_required", validateActivityForm("John", "   ", LocalTime.of(9, 0), LocalTime.of(17, 0)))
    }

    @Test
    fun testValidateActivityFormNullStartTime() {
        assertEquals("times_required", validateActivityForm("John", "Office", null, LocalTime.of(17, 0)))
    }

    @Test
    fun testValidateActivityFormNullEndTime() {
        assertEquals("times_required", validateActivityForm("John", "Office", LocalTime.of(9, 0), null))
    }

    @Test
    fun testValidateActivityFormBothTimesNull() {
        assertEquals("times_required", validateActivityForm("John", "Office", null, null))
    }

    @Test
    fun testValidateActivityFormFirstErrorWins() {
        // When both persons and location are blank, persons_required comes first
        assertEquals("persons_required", validateActivityForm("", "", null, null))
    }

    // --- Time range formatting tests ---

    @Test
    fun testFormatTimeRangeBothPresent() {
        val result = formatTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 30))
        assertEquals("09:00 - 17:30", result)
    }

    @Test
    fun testFormatTimeRangeStartNull() {
        val result = formatTimeRange(null, LocalTime.of(17, 0))
        assertEquals(" - 17:00", result)
    }

    @Test
    fun testFormatTimeRangeEndNull() {
        val result = formatTimeRange(LocalTime.of(9, 0), null)
        assertEquals("09:00 - ", result)
    }

    @Test
    fun testFormatTimeRangeBothNull() {
        val result = formatTimeRange(null, null)
        assertEquals(" - ", result)
    }

    @Test
    fun testFormatTimeRangeMidnightCrossing() {
        val result = formatTimeRange(LocalTime.of(23, 0), LocalTime.of(7, 0))
        assertEquals("23:00 - 07:00", result)
    }
}
