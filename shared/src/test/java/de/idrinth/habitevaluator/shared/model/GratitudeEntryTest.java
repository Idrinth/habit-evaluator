package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GratitudeEntryTest {

    @Test
    void testDefaultConstructor() {
        GratitudeEntry entry = new GratitudeEntry();
        assertNotNull(entry.getId());
        assertNotNull(entry.getCreatedAt());
        assertNotNull(entry.getEventDate());
    }

    @Test
    void testDescriptionConstructor() {
        GratitudeEntry entry = new GratitudeEntry("I'm grateful for my family");
        assertEquals("I'm grateful for my family", entry.getDescription());
        assertNotNull(entry.getId());
    }

    @Test
    void testDescriptionAndDateConstructor() {
        LocalDate date = LocalDate.of(2026, 2, 20);
        GratitudeEntry entry = new GratitudeEntry("I'm thankful for my health", date);
        assertEquals("I'm thankful for my health", entry.getDescription());
        assertEquals(date, entry.getEventDate());
    }

    @Test
    void testSetDescription() {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setDescription("Updated gratitude");
        assertEquals("Updated gratitude", entry.getDescription());
    }

    @Test
    void testSetEventDate() {
        GratitudeEntry entry = new GratitudeEntry();
        LocalDate date = LocalDate.of(2026, 6, 1);
        entry.setEventDate(date);
        assertEquals(date, entry.getEventDate());
    }

    @Test
    void testSetCreatedAt() {
        GratitudeEntry entry = new GratitudeEntry();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        entry.setCreatedAt(time);
        assertEquals(time, entry.getCreatedAt());
    }

    @Test
    void testSetUser() {
        GratitudeEntry entry = new GratitudeEntry();
        User user = new User("test", "pass");
        entry.setUser(user);
        assertSame(user, entry.getUser());
    }

    @Test
    void testSetId() {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setId("custom-id");
        assertEquals("custom-id", entry.getId());
    }

    @Test
    void testEqualsSameId() {
        GratitudeEntry e1 = new GratitudeEntry();
        GratitudeEntry e2 = new GratitudeEntry();
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void testEqualsDifferentId() {
        GratitudeEntry e1 = new GratitudeEntry();
        GratitudeEntry e2 = new GratitudeEntry();
        assertNotEquals(e1, e2);
    }

    @Test
    void testEqualsNull() {
        GratitudeEntry e = new GratitudeEntry();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        GratitudeEntry e = new GratitudeEntry();
        assertEquals(e, e);
    }

    @Test
    void testHashCodeConsistent() {
        GratitudeEntry e1 = new GratitudeEntry();
        GratitudeEntry e2 = new GratitudeEntry();
        e2.setId(e1.getId());
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testUserDefaultNull() {
        GratitudeEntry entry = new GratitudeEntry();
        assertNull(entry.getUser());
    }

    @Test
    void testReasonDefaultNull() {
        GratitudeEntry entry = new GratitudeEntry();
        assertNull(entry.getReason());
    }

    @Test
    void testSetReason() {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setReason("it allowed for a nice walk");
        assertEquals("it allowed for a nice walk", entry.getReason());
    }

    @Test
    void testSetReasonNull() {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setReason("some reason");
        entry.setReason(null);
        assertNull(entry.getReason());
    }
}
