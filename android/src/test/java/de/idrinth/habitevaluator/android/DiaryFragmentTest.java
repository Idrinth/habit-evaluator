package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import de.idrinth.habitevaluator.shared.model.EventSignificance;

import static org.junit.jupiter.api.Assertions.*;

class DiaryFragmentTest {

    @Test
    void testMapPositionToSignificanceMinor() {
        assertEquals(EventSignificance.MINOR, DiaryFragment.mapPositionToSignificance(0));
    }

    @Test
    void testMapPositionToSignificanceNormal() {
        assertEquals(EventSignificance.NORMAL, DiaryFragment.mapPositionToSignificance(1));
    }

    @Test
    void testMapPositionToSignificanceMajor() {
        assertEquals(EventSignificance.MAJOR, DiaryFragment.mapPositionToSignificance(2));
    }

    @Test
    void testMapPositionToSignificanceNegativeDefaultsToNormal() {
        assertEquals(EventSignificance.NORMAL, DiaryFragment.mapPositionToSignificance(-1));
    }

    @Test
    void testMapPositionToSignificanceOutOfRangeDefaultsToNormal() {
        assertEquals(EventSignificance.NORMAL, DiaryFragment.mapPositionToSignificance(99));
    }

    @Test
    void testDatePatternNotNull() {
        assertNotNull(DiaryFragment.DATE_PATTERN);
    }

    @Test
    void testTimePatternNotNull() {
        assertNotNull(DiaryFragment.TIME_PATTERN);
    }

    @Test
    void testDateFormatNotNull() {
        assertNotNull(DiaryFragment.DATE_FORMAT);
    }

    @Test
    void testTimeFormatNotNull() {
        assertNotNull(DiaryFragment.TIME_FORMAT);
    }

    @Test
    void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        assertEquals("2025-03-15", DiaryFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testDateFormatWithSingleDigitMonth() {
        LocalDate date = LocalDate.of(2025, 1, 5);
        assertEquals("2025-01-05", DiaryFragment.DATE_FORMAT.format(date));
    }

    @Test
    void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(14, 30);
        assertEquals("14:30", DiaryFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", DiaryFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testTimeFormatSingleDigitHour() {
        LocalTime time = LocalTime.of(9, 5);
        assertEquals("09:05", DiaryFragment.TIME_FORMAT.format(time));
    }

    @Test
    void testAllSignificanceValuesAreMapped() {
        // Verify that all three EventSignificance values are reachable
        EventSignificance minor = DiaryFragment.mapPositionToSignificance(0);
        EventSignificance normal = DiaryFragment.mapPositionToSignificance(1);
        EventSignificance major = DiaryFragment.mapPositionToSignificance(2);

        assertNotEquals(minor, normal);
        assertNotEquals(normal, major);
        assertNotEquals(minor, major);
    }
}
