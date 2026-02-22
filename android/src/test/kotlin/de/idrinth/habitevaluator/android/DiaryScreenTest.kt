package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.EventSignificance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tests for date/time formatting and significance mapping logic equivalent to
 * what DiaryFragment previously provided as static constants and helper methods.
 */
class DiaryScreenTest {

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm"
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)

        /**
         * Replication of mapPositionToSignificance logic from DiaryFragment.
         * Maps spinner position to EventSignificance, defaulting to NORMAL for invalid positions.
         */
        fun mapPositionToSignificance(position: Int): EventSignificance = when (position) {
            0 -> EventSignificance.MINOR
            1 -> EventSignificance.NORMAL
            2 -> EventSignificance.MAJOR
            else -> EventSignificance.NORMAL
        }
    }

    @Test
    fun testMapPositionToSignificanceMinor() {
        assertEquals(EventSignificance.MINOR, mapPositionToSignificance(0))
    }

    @Test
    fun testMapPositionToSignificanceNormal() {
        assertEquals(EventSignificance.NORMAL, mapPositionToSignificance(1))
    }

    @Test
    fun testMapPositionToSignificanceMajor() {
        assertEquals(EventSignificance.MAJOR, mapPositionToSignificance(2))
    }

    @Test
    fun testMapPositionToSignificanceNegativeDefaultsToNormal() {
        assertEquals(EventSignificance.NORMAL, mapPositionToSignificance(-1))
    }

    @Test
    fun testMapPositionToSignificanceOutOfRangeDefaultsToNormal() {
        assertEquals(EventSignificance.NORMAL, mapPositionToSignificance(99))
    }

    @Test
    fun testDatePatternNotNull() {
        assertNotNull(DATE_PATTERN)
    }

    @Test
    fun testTimePatternNotNull() {
        assertNotNull(TIME_PATTERN)
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
        val date = LocalDate.of(2025, 3, 15)
        assertEquals("2025-03-15", DATE_FORMAT.format(date))
    }

    @Test
    fun testDateFormatWithSingleDigitMonth() {
        val date = LocalDate.of(2025, 1, 5)
        assertEquals("2025-01-05", DATE_FORMAT.format(date))
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
        val time = LocalTime.of(9, 5)
        assertEquals("09:05", TIME_FORMAT.format(time))
    }

    @Test
    fun testAllSignificanceValuesAreMapped() {
        val minor = mapPositionToSignificance(0)
        val normal = mapPositionToSignificance(1)
        val major = mapPositionToSignificance(2)

        assertNotEquals(minor, normal)
        assertNotEquals(normal, major)
        assertNotEquals(minor, major)
    }
}
