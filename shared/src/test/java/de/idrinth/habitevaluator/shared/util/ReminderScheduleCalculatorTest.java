package de.idrinth.habitevaluator.shared.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReminderScheduleCalculatorTest {

    @Test
    void testParseTimeWithValidTime() {
        int[] result = ReminderScheduleCalculator.parseTime("14:30");
        assertEquals(14, result[0]);
        assertEquals(30, result[1]);
    }

    @Test
    void testParseTimeWithMidnight() {
        int[] result = ReminderScheduleCalculator.parseTime("00:00");
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeWithEndOfDay() {
        int[] result = ReminderScheduleCalculator.parseTime("23:59");
        assertEquals(23, result[0]);
        assertEquals(59, result[1]);
    }

    @Test
    void testParseTimeWithInvalidStringDefaultsToEight() {
        int[] result = ReminderScheduleCalculator.parseTime("invalid");
        assertEquals(8, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeWithEmptyStringDefaultsToEight() {
        int[] result = ReminderScheduleCalculator.parseTime("");
        assertEquals(8, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testParseTimeWithNullDefaultsToEight() {
        int[] result = ReminderScheduleCalculator.parseTime(null);
        assertEquals(8, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testCalculateEmotionReminderTimesReturnsCorrectCount() {
        List<String> times = ReminderScheduleCalculator.calculateEmotionReminderTimes("07:00", "22:00", 3);
        assertEquals(3, times.size());
    }

    @Test
    void testCalculateEmotionReminderTimesCapsAtTen() {
        List<String> times = ReminderScheduleCalculator.calculateEmotionReminderTimes("07:00", "22:00", 15);
        assertEquals(10, times.size());
    }

    @Test
    void testCalculateEmotionReminderTimesWithEndBeforeStart() {
        List<String> times = ReminderScheduleCalculator.calculateEmotionReminderTimes("22:00", "07:00", 2);
        assertEquals(2, times.size());
    }

    @Test
    void testCalculateEmotionReminderTimesFormatsCorrectly() {
        List<String> times = ReminderScheduleCalculator.calculateEmotionReminderTimes("07:00", "22:00", 1);
        assertEquals(1, times.size());
        assertTrue(times.get(0).matches("\\d{2}:\\d{2}"), "Time should match HH:MM format");
    }
}
