package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import de.idrinth.habitevaluator.shared.model.EventSignificance;

import static org.junit.Assert.*;

public class DiaryFragmentTest {

    @Test
    public void testMapPositionToSignificanceMinor() {
        assertEquals(EventSignificance.MINOR, DiaryFragment.mapPositionToSignificance(0));
    }

    @Test
    public void testMapPositionToSignificanceNormal() {
        assertEquals(EventSignificance.NORMAL, DiaryFragment.mapPositionToSignificance(1));
    }

    @Test
    public void testMapPositionToSignificanceMajor() {
        assertEquals(EventSignificance.MAJOR, DiaryFragment.mapPositionToSignificance(2));
    }

    @Test
    public void testMapPositionToSignificanceNegativeDefaultsToNormal() {
        assertEquals(EventSignificance.NORMAL, DiaryFragment.mapPositionToSignificance(-1));
    }

    @Test
    public void testMapPositionToSignificanceOutOfRangeDefaultsToNormal() {
        assertEquals(EventSignificance.NORMAL, DiaryFragment.mapPositionToSignificance(99));
    }

    @Test
    public void testDatePatternNotNull() {
        assertNotNull(DiaryFragment.DATE_PATTERN);
    }

    @Test
    public void testTimePatternNotNull() {
        assertNotNull(DiaryFragment.TIME_PATTERN);
    }

    @Test
    public void testDateFormatNotNull() {
        assertNotNull(DiaryFragment.DATE_FORMAT);
    }

    @Test
    public void testTimeFormatNotNull() {
        assertNotNull(DiaryFragment.TIME_FORMAT);
    }

    @Test
    public void testDateFormatProducesExpectedOutput() {
        LocalDate date = LocalDate.of(2025, 3, 15);
        assertEquals("2025-03-15", DiaryFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testDateFormatWithSingleDigitMonth() {
        LocalDate date = LocalDate.of(2025, 1, 5);
        assertEquals("2025-01-05", DiaryFragment.DATE_FORMAT.format(date));
    }

    @Test
    public void testTimeFormatProducesExpectedOutput() {
        LocalTime time = LocalTime.of(14, 30);
        assertEquals("14:30", DiaryFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatMidnight() {
        LocalTime time = LocalTime.of(0, 0);
        assertEquals("00:00", DiaryFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testTimeFormatSingleDigitHour() {
        LocalTime time = LocalTime.of(9, 5);
        assertEquals("09:05", DiaryFragment.TIME_FORMAT.format(time));
    }

    @Test
    public void testAllSignificanceValuesAreMapped() {
        // Verify that all three EventSignificance values are reachable
        EventSignificance minor = DiaryFragment.mapPositionToSignificance(0);
        EventSignificance normal = DiaryFragment.mapPositionToSignificance(1);
        EventSignificance major = DiaryFragment.mapPositionToSignificance(2);

        assertNotEquals(minor, normal);
        assertNotEquals(normal, major);
        assertNotEquals(minor, major);
    }
}
