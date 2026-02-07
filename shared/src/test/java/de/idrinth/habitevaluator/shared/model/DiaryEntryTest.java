package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DiaryEntryTest {

    @Test
    void testDefaultConstructor() {
        DiaryEntry entry = new DiaryEntry();
        assertNotNull(entry.getId());
        assertNotNull(entry.getCreatedAt());
        assertNotNull(entry.getEventDate());
        assertEquals(EventSignificance.NORMAL, entry.getSignificance());
    }

    @Test
    void testDescriptionSignificanceConstructor() {
        DiaryEntry entry = new DiaryEntry("Got promoted", EventSignificance.MAJOR);
        assertEquals("Got promoted", entry.getDescription());
        assertEquals(EventSignificance.MAJOR, entry.getSignificance());
        assertNotNull(entry.getId());
    }

    @Test
    void testFullConstructor() {
        LocalDate date = LocalDate.of(2026, 1, 20);
        DiaryEntry entry = new DiaryEntry("Nice walk", EventSignificance.MINOR, date);
        assertEquals("Nice walk", entry.getDescription());
        assertEquals(EventSignificance.MINOR, entry.getSignificance());
        assertEquals(date, entry.getEventDate());
    }

    @Test
    void testGetPointsMinor() {
        DiaryEntry entry = new DiaryEntry("test", EventSignificance.MINOR);
        assertEquals(1, entry.getPoints());
    }

    @Test
    void testGetPointsNormal() {
        DiaryEntry entry = new DiaryEntry("test", EventSignificance.NORMAL);
        assertEquals(2, entry.getPoints());
    }

    @Test
    void testGetPointsMajor() {
        DiaryEntry entry = new DiaryEntry("test", EventSignificance.MAJOR);
        assertEquals(4, entry.getPoints());
    }

    @Test
    void testSetDescription() {
        DiaryEntry entry = new DiaryEntry();
        entry.setDescription("Updated");
        assertEquals("Updated", entry.getDescription());
    }

    @Test
    void testSetSignificance() {
        DiaryEntry entry = new DiaryEntry();
        entry.setSignificance(EventSignificance.MAJOR);
        assertEquals(EventSignificance.MAJOR, entry.getSignificance());
    }

    @Test
    void testSetEventDate() {
        DiaryEntry entry = new DiaryEntry();
        LocalDate date = LocalDate.of(2026, 6, 1);
        entry.setEventDate(date);
        assertEquals(date, entry.getEventDate());
    }

    @Test
    void testSetCreatedAt() {
        DiaryEntry entry = new DiaryEntry();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        entry.setCreatedAt(time);
        assertEquals(time, entry.getCreatedAt());
    }

    @Test
    void testSetUser() {
        DiaryEntry entry = new DiaryEntry();
        User user = new User("test", "pass");
        entry.setUser(user);
        assertSame(user, entry.getUser());
    }

    @Test
    void testEqualsSameId() {
        DiaryEntry e1 = new DiaryEntry();
        DiaryEntry e2 = new DiaryEntry();
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void testEqualsDifferentId() {
        DiaryEntry e1 = new DiaryEntry();
        DiaryEntry e2 = new DiaryEntry();
        assertNotEquals(e1, e2);
    }

    @Test
    void testEqualsNull() {
        DiaryEntry e = new DiaryEntry();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        DiaryEntry e = new DiaryEntry();
        assertEquals(e, e);
    }

    @Test
    void testHashCodeConsistent() {
        DiaryEntry e1 = new DiaryEntry();
        DiaryEntry e2 = new DiaryEntry();
        e2.setId(e1.getId());
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testDiaryReferenceConstructor() {
        DiaryReference ref = new DiaryReference("Test description");
        DiaryEntry entry = new DiaryEntry(ref, EventSignificance.MAJOR);
        assertEquals("Test description", entry.getDescription());
        assertEquals(EventSignificance.MAJOR, entry.getSignificance());
        assertSame(ref, entry.getDiaryReference());
    }

    @Test
    void testDiaryReferenceWithDateConstructor() {
        DiaryReference ref = new DiaryReference("Test description");
        LocalDate date = LocalDate.of(2026, 1, 20);
        DiaryEntry entry = new DiaryEntry(ref, EventSignificance.MINOR, date);
        assertEquals("Test description", entry.getDescription());
        assertEquals(EventSignificance.MINOR, entry.getSignificance());
        assertEquals(date, entry.getEventDate());
        assertSame(ref, entry.getDiaryReference());
    }

    @Test
    void testGetDescriptionFromReference() {
        DiaryReference ref = new DiaryReference("Referenced description");
        DiaryEntry entry = new DiaryEntry();
        entry.setDiaryReference(ref);
        assertEquals("Referenced description", entry.getDescription());
    }

    @Test
    void testGetDescriptionFallsBackToLegacy() {
        DiaryEntry entry = new DiaryEntry();
        entry.setLegacyDescription("Legacy description");
        assertNull(entry.getDiaryReference());
        assertEquals("Legacy description", entry.getDescription());
    }

    @Test
    void testReferenceOverridesLegacy() {
        DiaryReference ref = new DiaryReference("From reference");
        DiaryEntry entry = new DiaryEntry();
        entry.setLegacyDescription("From legacy");
        entry.setDiaryReference(ref);
        assertEquals("From reference", entry.getDescription());
    }

    @Test
    void testNeedsMigrationWithLegacyDescription() {
        DiaryEntry entry = new DiaryEntry();
        entry.setLegacyDescription("Needs migration");
        assertTrue(entry.needsMigration());
    }

    @Test
    void testNeedsMigrationWithReference() {
        DiaryReference ref = new DiaryReference("Has reference");
        DiaryEntry entry = new DiaryEntry();
        entry.setDiaryReference(ref);
        assertFalse(entry.needsMigration());
    }

    @Test
    void testNeedsMigrationWithEmptyLegacy() {
        DiaryEntry entry = new DiaryEntry();
        entry.setLegacyDescription("");
        assertFalse(entry.needsMigration());
    }

    @Test
    void testNeedsMigrationWithNullLegacy() {
        DiaryEntry entry = new DiaryEntry();
        entry.setLegacyDescription(null);
        assertFalse(entry.needsMigration());
    }

    @Test
    void testStartTimeAndEndTimeDefaultNull() {
        DiaryEntry entry = new DiaryEntry();
        assertNull(entry.getStartTime());
        assertNull(entry.getEndTime());
    }

    @Test
    void testSetStartTime() {
        DiaryEntry entry = new DiaryEntry();
        LocalTime time = LocalTime.of(14, 30);
        entry.setStartTime(time);
        assertEquals(time, entry.getStartTime());
    }

    @Test
    void testSetEndTime() {
        DiaryEntry entry = new DiaryEntry();
        LocalTime time = LocalTime.of(16, 45);
        entry.setEndTime(time);
        assertEquals(time, entry.getEndTime());
    }

    @Test
    void testGetDurationMinutesNormal() {
        DiaryEntry entry = new DiaryEntry();
        entry.setStartTime(LocalTime.of(10, 0));
        entry.setEndTime(LocalTime.of(11, 30));
        assertEquals(90, entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesMidnightCrossing() {
        DiaryEntry entry = new DiaryEntry();
        entry.setStartTime(LocalTime.of(23, 0));
        entry.setEndTime(LocalTime.of(1, 0));
        assertEquals(120, entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullWhenNoStartTime() {
        DiaryEntry entry = new DiaryEntry();
        entry.setEndTime(LocalTime.of(12, 0));
        assertNull(entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullWhenNoEndTime() {
        DiaryEntry entry = new DiaryEntry();
        entry.setStartTime(LocalTime.of(12, 0));
        assertNull(entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullWhenBothNull() {
        DiaryEntry entry = new DiaryEntry();
        assertNull(entry.getDurationMinutes());
    }
}
