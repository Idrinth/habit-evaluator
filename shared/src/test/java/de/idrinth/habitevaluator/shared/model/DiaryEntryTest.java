package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
