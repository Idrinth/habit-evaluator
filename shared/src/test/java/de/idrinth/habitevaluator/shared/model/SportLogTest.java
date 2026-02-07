package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SportLogTest {

    @Test
    void testDefaultConstructor() {
        SportLog log = new SportLog();
        assertNotNull(log.getId());
        assertNotNull(log.getCreatedAt());
        assertNotNull(log.getDate());
    }

    @Test
    void testBasicConstructor() {
        SportLog log = new SportLog("Running", 5.0, "km",
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        assertEquals("Running", log.getName());
        assertEquals(5.0, log.getMeasurement());
        assertEquals("km", log.getMeasurementUnit());
        assertEquals(LocalTime.of(8, 0), log.getStartTime());
        assertEquals(LocalTime.of(9, 0), log.getEndTime());
        assertNotNull(log.getId());
    }

    @Test
    void testFullConstructor() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        SportLog log = new SportLog("Swimming", 30.0, "laps",
                LocalTime.of(7, 0), LocalTime.of(8, 30), date);
        assertEquals("Swimming", log.getName());
        assertEquals(30.0, log.getMeasurement());
        assertEquals("laps", log.getMeasurementUnit());
        assertEquals(LocalTime.of(7, 0), log.getStartTime());
        assertEquals(LocalTime.of(8, 30), log.getEndTime());
        assertEquals(date, log.getDate());
    }

    @Test
    void testGetDurationHoursNormalRange() {
        SportLog log = new SportLog("Running", 5.0, "km",
                LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertEquals(2.0, log.getDurationHours(), 0.01);
    }

    @Test
    void testGetDurationHoursCrossingMidnight() {
        SportLog log = new SportLog("Late run", 10.0, "km",
                LocalTime.of(23, 0), LocalTime.of(1, 0));
        assertEquals(2.0, log.getDurationHours(), 0.01);
    }

    @Test
    void testGetDurationHoursWithMinutes() {
        SportLog log = new SportLog("Cycling", 25.0, "km",
                LocalTime.of(14, 30), LocalTime.of(16, 0));
        assertEquals(1.5, log.getDurationHours(), 0.01);
    }

    @Test
    void testGetDurationHoursNullStartTime() {
        SportLog log = new SportLog();
        log.setEndTime(LocalTime.of(10, 0));
        assertEquals(0, log.getDurationHours());
    }

    @Test
    void testGetDurationHoursNullEndTime() {
        SportLog log = new SportLog();
        log.setStartTime(LocalTime.of(10, 0));
        assertEquals(0, log.getDurationHours());
    }

    @Test
    void testGetDurationHoursBothNull() {
        SportLog log = new SportLog();
        assertEquals(0, log.getDurationHours());
    }

    @Test
    void testSetName() {
        SportLog log = new SportLog();
        log.setName("Yoga");
        assertEquals("Yoga", log.getName());
    }

    @Test
    void testSetMeasurement() {
        SportLog log = new SportLog();
        log.setMeasurement(42.0);
        assertEquals(42.0, log.getMeasurement());
    }

    @Test
    void testSetMeasurementUnit() {
        SportLog log = new SportLog();
        log.setMeasurementUnit("repetitions");
        assertEquals("repetitions", log.getMeasurementUnit());
    }

    @Test
    void testSetStartTime() {
        SportLog log = new SportLog();
        log.setStartTime(LocalTime.of(9, 30));
        assertEquals(LocalTime.of(9, 30), log.getStartTime());
    }

    @Test
    void testSetEndTime() {
        SportLog log = new SportLog();
        log.setEndTime(LocalTime.of(11, 0));
        assertEquals(LocalTime.of(11, 0), log.getEndTime());
    }

    @Test
    void testSetDate() {
        SportLog log = new SportLog();
        LocalDate date = LocalDate.of(2026, 3, 15);
        log.setDate(date);
        assertEquals(date, log.getDate());
    }

    @Test
    void testSetNotes() {
        SportLog log = new SportLog();
        log.setNotes("Great workout");
        assertEquals("Great workout", log.getNotes());
    }

    @Test
    void testSetCreatedAt() {
        SportLog log = new SportLog();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        log.setCreatedAt(time);
        assertEquals(time, log.getCreatedAt());
    }

    @Test
    void testSetUser() {
        SportLog log = new SportLog();
        User user = new User("test", "pass");
        log.setUser(user);
        assertSame(user, log.getUser());
    }

    @Test
    void testEqualsSameId() {
        SportLog l1 = new SportLog();
        SportLog l2 = new SportLog();
        l2.setId(l1.getId());
        assertEquals(l1, l2);
    }

    @Test
    void testEqualsDifferentId() {
        SportLog l1 = new SportLog();
        SportLog l2 = new SportLog();
        assertNotEquals(l1, l2);
    }

    @Test
    void testEqualsNull() {
        SportLog l = new SportLog();
        assertNotEquals(null, l);
    }

    @Test
    void testEqualsSameObject() {
        SportLog l = new SportLog();
        assertEquals(l, l);
    }

    @Test
    void testHashCodeConsistent() {
        SportLog l1 = new SportLog();
        SportLog l2 = new SportLog();
        l2.setId(l1.getId());
        assertEquals(l1.hashCode(), l2.hashCode());
    }
}
