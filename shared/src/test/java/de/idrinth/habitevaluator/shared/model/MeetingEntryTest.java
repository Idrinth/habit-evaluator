package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class MeetingEntryTest {

    @Test
    void testDefaultConstructor() {
        MeetingEntry entry = new MeetingEntry();
        assertNotNull(entry.getId());
        assertNotNull(entry.getCreatedAt());
        assertNotNull(entry.getDate());
    }

    @Test
    void testParameterizedConstructor() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice, Bob",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertEquals("Office", entry.getPlace());
        assertEquals("Alice, Bob", entry.getAttendants());
        assertEquals(LocalTime.of(10, 0), entry.getStartTime());
        assertEquals(LocalTime.of(11, 0), entry.getEndTime());
        assertNotNull(entry.getId());
    }

    @Test
    void testFullConstructor() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        MeetingEntry entry = new MeetingEntry("Café", "Charlie",
                LocalTime.of(14, 0), LocalTime.of(15, 30), date);
        assertEquals("Café", entry.getPlace());
        assertEquals("Charlie", entry.getAttendants());
        assertEquals(LocalTime.of(14, 0), entry.getStartTime());
        assertEquals(LocalTime.of(15, 30), entry.getEndTime());
        assertEquals(date, entry.getDate());
    }

    @Test
    void testGetDurationMinutesNormalRange() {
        MeetingEntry entry = new MeetingEntry("Office", "Alice",
                LocalTime.of(10, 0), LocalTime.of(11, 30));
        assertEquals(90, entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesCrossingMidnight() {
        MeetingEntry entry = new MeetingEntry("Bar", "Bob",
                LocalTime.of(23, 0), LocalTime.of(1, 0));
        assertEquals(120, entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullStartTime() {
        MeetingEntry entry = new MeetingEntry();
        entry.setEndTime(LocalTime.of(11, 0));
        assertNull(entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullEndTime() {
        MeetingEntry entry = new MeetingEntry();
        entry.setStartTime(LocalTime.of(10, 0));
        assertNull(entry.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesBothNull() {
        MeetingEntry entry = new MeetingEntry();
        assertNull(entry.getDurationMinutes());
    }

    @Test
    void testSetPlace() {
        MeetingEntry entry = new MeetingEntry();
        entry.setPlace("Conference Room");
        assertEquals("Conference Room", entry.getPlace());
    }

    @Test
    void testSetAttendants() {
        MeetingEntry entry = new MeetingEntry();
        entry.setAttendants("Alice, Bob, Charlie");
        assertEquals("Alice, Bob, Charlie", entry.getAttendants());
    }

    @Test
    void testSetStartTime() {
        MeetingEntry entry = new MeetingEntry();
        entry.setStartTime(LocalTime.of(9, 0));
        assertEquals(LocalTime.of(9, 0), entry.getStartTime());
    }

    @Test
    void testSetEndTime() {
        MeetingEntry entry = new MeetingEntry();
        entry.setEndTime(LocalTime.of(17, 0));
        assertEquals(LocalTime.of(17, 0), entry.getEndTime());
    }

    @Test
    void testSetDate() {
        MeetingEntry entry = new MeetingEntry();
        LocalDate date = LocalDate.of(2026, 3, 15);
        entry.setDate(date);
        assertEquals(date, entry.getDate());
    }

    @Test
    void testSetCreatedAt() {
        MeetingEntry entry = new MeetingEntry();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        entry.setCreatedAt(time);
        assertEquals(time, entry.getCreatedAt());
    }

    @Test
    void testSetUser() {
        MeetingEntry entry = new MeetingEntry();
        User user = new User("test", "pass");
        entry.setUser(user);
        assertSame(user, entry.getUser());
    }

    @Test
    void testEqualsSameId() {
        MeetingEntry e1 = new MeetingEntry();
        MeetingEntry e2 = new MeetingEntry();
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void testEqualsDifferentId() {
        MeetingEntry e1 = new MeetingEntry();
        MeetingEntry e2 = new MeetingEntry();
        assertNotEquals(e1, e2);
    }

    @Test
    void testEqualsNull() {
        MeetingEntry e = new MeetingEntry();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        MeetingEntry e = new MeetingEntry();
        assertEquals(e, e);
    }

    @Test
    void testHashCodeConsistent() {
        MeetingEntry e1 = new MeetingEntry();
        MeetingEntry e2 = new MeetingEntry();
        e2.setId(e1.getId());
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
