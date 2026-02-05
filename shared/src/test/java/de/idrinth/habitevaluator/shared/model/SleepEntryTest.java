package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SleepEntryTest {

    @Test
    void testDefaultConstructor() {
        SleepEntry entry = new SleepEntry();
        assertNotNull(entry.getId());
        assertNotNull(entry.getCreatedAt());
        assertNotNull(entry.getDate());
    }

    @Test
    void testFromUntilConstructor() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        assertEquals(LocalTime.of(22, 0), entry.getFromTime());
        assertEquals(LocalTime.of(6, 0), entry.getUntilTime());
        assertNotNull(entry.getId());
    }

    @Test
    void testFullConstructor() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 0), LocalTime.of(7, 0), date);
        assertEquals(LocalTime.of(23, 0), entry.getFromTime());
        assertEquals(LocalTime.of(7, 0), entry.getUntilTime());
        assertEquals(date, entry.getDate());
    }

    @Test
    void testGetHoursNormalRange() {
        SleepEntry entry = new SleepEntry(LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertEquals(2.0, entry.getHours(), 0.01);
    }

    @Test
    void testGetHoursCrossingMidnight() {
        SleepEntry entry = new SleepEntry(LocalTime.of(22, 0), LocalTime.of(6, 0));
        assertEquals(8.0, entry.getHours(), 0.01);
    }

    @Test
    void testGetHoursWithMinutes() {
        SleepEntry entry = new SleepEntry(LocalTime.of(23, 30), LocalTime.of(7, 0));
        assertEquals(7.5, entry.getHours(), 0.01);
    }

    @Test
    void testGetHoursNullFromTime() {
        SleepEntry entry = new SleepEntry();
        entry.setUntilTime(LocalTime.of(6, 0));
        assertEquals(0, entry.getHours());
    }

    @Test
    void testGetHoursNullUntilTime() {
        SleepEntry entry = new SleepEntry();
        entry.setFromTime(LocalTime.of(22, 0));
        assertEquals(0, entry.getHours());
    }

    @Test
    void testGetHoursBothNull() {
        SleepEntry entry = new SleepEntry();
        assertEquals(0, entry.getHours());
    }

    @Test
    void testSetFromTime() {
        SleepEntry entry = new SleepEntry();
        entry.setFromTime(LocalTime.of(21, 0));
        assertEquals(LocalTime.of(21, 0), entry.getFromTime());
    }

    @Test
    void testSetUntilTime() {
        SleepEntry entry = new SleepEntry();
        entry.setUntilTime(LocalTime.of(5, 30));
        assertEquals(LocalTime.of(5, 30), entry.getUntilTime());
    }

    @Test
    void testSetDate() {
        SleepEntry entry = new SleepEntry();
        LocalDate date = LocalDate.of(2026, 3, 15);
        entry.setDate(date);
        assertEquals(date, entry.getDate());
    }

    @Test
    void testSetNotes() {
        SleepEntry entry = new SleepEntry();
        entry.setNotes("Slept well");
        assertEquals("Slept well", entry.getNotes());
    }

    @Test
    void testSetCreatedAt() {
        SleepEntry entry = new SleepEntry();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        entry.setCreatedAt(time);
        assertEquals(time, entry.getCreatedAt());
    }

    @Test
    void testSetUser() {
        SleepEntry entry = new SleepEntry();
        User user = new User("test", "pass");
        entry.setUser(user);
        assertSame(user, entry.getUser());
    }

    @Test
    void testEqualsSameId() {
        SleepEntry e1 = new SleepEntry();
        SleepEntry e2 = new SleepEntry();
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void testEqualsDifferentId() {
        SleepEntry e1 = new SleepEntry();
        SleepEntry e2 = new SleepEntry();
        assertNotEquals(e1, e2);
    }

    @Test
    void testEqualsNull() {
        SleepEntry e = new SleepEntry();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        SleepEntry e = new SleepEntry();
        assertEquals(e, e);
    }

    @Test
    void testHashCodeConsistent() {
        SleepEntry e1 = new SleepEntry();
        SleepEntry e2 = new SleepEntry();
        e2.setId(e1.getId());
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
