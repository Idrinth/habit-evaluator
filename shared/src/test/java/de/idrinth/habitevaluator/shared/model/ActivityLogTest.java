package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ActivityLogTest {

    @Test
    void testDefaultConstructor() {
        ActivityLog log = new ActivityLog();
        assertNotNull(log.getId());
        assertNotNull(log.getCreatedAt());
        assertNotNull(log.getDate());
    }

    @Test
    void testParameterizedConstructor() {
        ActivityLog log = new ActivityLog("Alice, Bob", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertEquals("Alice, Bob", log.getPersons());
        assertEquals("Office", log.getLocation());
        assertEquals(LocalTime.of(10, 0), log.getStartTime());
        assertEquals(LocalTime.of(11, 0), log.getEndTime());
        assertNotNull(log.getId());
    }

    @Test
    void testFullConstructor() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        ActivityLog log = new ActivityLog("Charlie", "Café",
                LocalTime.of(14, 0), LocalTime.of(15, 30), date);
        assertEquals("Charlie", log.getPersons());
        assertEquals("Café", log.getLocation());
        assertEquals(LocalTime.of(14, 0), log.getStartTime());
        assertEquals(LocalTime.of(15, 30), log.getEndTime());
        assertEquals(date, log.getDate());
    }

    @Test
    void testGetDurationMinutesNormalRange() {
        ActivityLog log = new ActivityLog("Alice", "Office",
                LocalTime.of(10, 0), LocalTime.of(11, 30));
        assertEquals(90, log.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesCrossingMidnight() {
        ActivityLog log = new ActivityLog("Bob", "Bar",
                LocalTime.of(23, 0), LocalTime.of(1, 0));
        assertEquals(120, log.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullStartTime() {
        ActivityLog log = new ActivityLog();
        log.setEndTime(LocalTime.of(11, 0));
        assertNull(log.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesNullEndTime() {
        ActivityLog log = new ActivityLog();
        log.setStartTime(LocalTime.of(10, 0));
        assertNull(log.getDurationMinutes());
    }

    @Test
    void testGetDurationMinutesBothNull() {
        ActivityLog log = new ActivityLog();
        assertNull(log.getDurationMinutes());
    }

    @Test
    void testSetPersons() {
        ActivityLog log = new ActivityLog();
        log.setPersons("Alice, Bob, Charlie");
        assertEquals("Alice, Bob, Charlie", log.getPersons());
    }

    @Test
    void testSetLocation() {
        ActivityLog log = new ActivityLog();
        log.setLocation("Conference Room");
        assertEquals("Conference Room", log.getLocation());
    }

    @Test
    void testSetStartTime() {
        ActivityLog log = new ActivityLog();
        log.setStartTime(LocalTime.of(9, 0));
        assertEquals(LocalTime.of(9, 0), log.getStartTime());
    }

    @Test
    void testSetEndTime() {
        ActivityLog log = new ActivityLog();
        log.setEndTime(LocalTime.of(17, 0));
        assertEquals(LocalTime.of(17, 0), log.getEndTime());
    }

    @Test
    void testSetDate() {
        ActivityLog log = new ActivityLog();
        LocalDate date = LocalDate.of(2026, 3, 15);
        log.setDate(date);
        assertEquals(date, log.getDate());
    }

    @Test
    void testSetActivity() {
        ActivityLog log = new ActivityLog();
        log.setActivity("Team standup meeting");
        assertEquals("Team standup meeting", log.getActivity());
    }

    @Test
    void testSetCreatedAt() {
        ActivityLog log = new ActivityLog();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        log.setCreatedAt(time);
        assertEquals(time, log.getCreatedAt());
    }

    @Test
    void testSetUser() {
        ActivityLog log = new ActivityLog();
        User user = new User("test", "pass");
        log.setUser(user);
        assertSame(user, log.getUser());
    }

    @Test
    void testEqualsSameId() {
        ActivityLog l1 = new ActivityLog();
        ActivityLog l2 = new ActivityLog();
        l2.setId(l1.getId());
        assertEquals(l1, l2);
    }

    @Test
    void testEqualsDifferentId() {
        ActivityLog l1 = new ActivityLog();
        ActivityLog l2 = new ActivityLog();
        assertNotEquals(l1, l2);
    }

    @Test
    void testEqualsNull() {
        ActivityLog l = new ActivityLog();
        assertNotEquals(null, l);
    }

    @Test
    void testEqualsSameObject() {
        ActivityLog l = new ActivityLog();
        assertEquals(l, l);
    }

    @Test
    void testHashCodeConsistent() {
        ActivityLog l1 = new ActivityLog();
        ActivityLog l2 = new ActivityLog();
        l2.setId(l1.getId());
        assertEquals(l1.hashCode(), l2.hashCode());
    }
}
