package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReminderSettingsTest {

    @Test
    void testDefaultConstructor() {
        ReminderSettings settings = new ReminderSettings();
        assertNotNull(settings.getId());
        assertFalse(settings.isSleepReminderEnabled());
        assertFalse(settings.isDiaryReminderEnabled());
        assertFalse(settings.isGratitudeReminderEnabled());
        assertFalse(settings.isEmotionReminderEnabled());
    }

    @Test
    void testDefaultTimes() {
        ReminderSettings settings = new ReminderSettings();
        assertEquals(LocalTime.of(8, 0), settings.getSleepReminderTime());
        assertEquals(LocalTime.of(20, 0), settings.getDiaryReminderTime());
        assertEquals(LocalTime.of(8, 0), settings.getGratitudeReminderTime());
        assertEquals(LocalTime.of(7, 0), settings.getWakingHoursStart());
        assertEquals(LocalTime.of(22, 0), settings.getWakingHoursEnd());
    }

    @Test
    void testDefaultEmotionCount() {
        ReminderSettings settings = new ReminderSettings();
        assertEquals(3, settings.getEmotionReminderCount());
    }

    @Test
    void testSetSleepReminderEnabled() {
        ReminderSettings settings = new ReminderSettings();
        settings.setSleepReminderEnabled(true);
        assertTrue(settings.isSleepReminderEnabled());
    }

    @Test
    void testSetSleepReminderTime() {
        ReminderSettings settings = new ReminderSettings();
        settings.setSleepReminderTime(LocalTime.of(9, 30));
        assertEquals(LocalTime.of(9, 30), settings.getSleepReminderTime());
    }

    @Test
    void testSetDiaryReminderEnabled() {
        ReminderSettings settings = new ReminderSettings();
        settings.setDiaryReminderEnabled(true);
        assertTrue(settings.isDiaryReminderEnabled());
    }

    @Test
    void testSetDiaryReminderTime() {
        ReminderSettings settings = new ReminderSettings();
        settings.setDiaryReminderTime(LocalTime.of(21, 0));
        assertEquals(LocalTime.of(21, 0), settings.getDiaryReminderTime());
    }

    @Test
    void testSetGratitudeReminderEnabled() {
        ReminderSettings settings = new ReminderSettings();
        settings.setGratitudeReminderEnabled(true);
        assertTrue(settings.isGratitudeReminderEnabled());
    }

    @Test
    void testSetGratitudeReminderTime() {
        ReminderSettings settings = new ReminderSettings();
        settings.setGratitudeReminderTime(LocalTime.of(7, 30));
        assertEquals(LocalTime.of(7, 30), settings.getGratitudeReminderTime());
    }

    @Test
    void testSetEmotionReminderEnabled() {
        ReminderSettings settings = new ReminderSettings();
        settings.setEmotionReminderEnabled(true);
        assertTrue(settings.isEmotionReminderEnabled());
    }

    @Test
    void testSetEmotionReminderCount() {
        ReminderSettings settings = new ReminderSettings();
        settings.setEmotionReminderCount(5);
        assertEquals(5, settings.getEmotionReminderCount());
    }

    @Test
    void testEmotionReminderCountClampsLow() {
        ReminderSettings settings = new ReminderSettings();
        settings.setEmotionReminderCount(0);
        assertEquals(1, settings.getEmotionReminderCount());
    }

    @Test
    void testEmotionReminderCountClampsHigh() {
        ReminderSettings settings = new ReminderSettings();
        settings.setEmotionReminderCount(20);
        assertEquals(10, settings.getEmotionReminderCount());
    }

    @Test
    void testSetWakingHoursStart() {
        ReminderSettings settings = new ReminderSettings();
        settings.setWakingHoursStart(LocalTime.of(6, 0));
        assertEquals(LocalTime.of(6, 0), settings.getWakingHoursStart());
    }

    @Test
    void testSetWakingHoursEnd() {
        ReminderSettings settings = new ReminderSettings();
        settings.setWakingHoursEnd(LocalTime.of(23, 0));
        assertEquals(LocalTime.of(23, 0), settings.getWakingHoursEnd());
    }

    @Test
    void testSetUser() {
        ReminderSettings settings = new ReminderSettings();
        User user = new User("test", "pass");
        settings.setUser(user);
        assertSame(user, settings.getUser());
    }

    @Test
    void testSetId() {
        ReminderSettings settings = new ReminderSettings();
        settings.setId("custom-id");
        assertEquals("custom-id", settings.getId());
    }

    @Test
    void testEqualsSameId() {
        ReminderSettings s1 = new ReminderSettings();
        ReminderSettings s2 = new ReminderSettings();
        s2.setId(s1.getId());
        assertEquals(s1, s2);
    }

    @Test
    void testEqualsDifferentId() {
        ReminderSettings s1 = new ReminderSettings();
        ReminderSettings s2 = new ReminderSettings();
        assertNotEquals(s1, s2);
    }

    @Test
    void testEqualsNull() {
        ReminderSettings s = new ReminderSettings();
        assertNotEquals(null, s);
    }

    @Test
    void testEqualsSameObject() {
        ReminderSettings s = new ReminderSettings();
        assertEquals(s, s);
    }

    @Test
    void testHashCodeConsistent() {
        ReminderSettings s1 = new ReminderSettings();
        ReminderSettings s2 = new ReminderSettings();
        s2.setId(s1.getId());
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
