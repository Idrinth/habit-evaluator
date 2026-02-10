package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MedicationLogTest {

    @Test
    void testDefaultConstructor() {
        MedicationLog log = new MedicationLog();
        assertNotNull(log.getId());
        assertEquals(36, log.getId().length());
        assertNotNull(log.getCreatedAt());
        assertNotNull(log.getTakenAt());
        assertNull(log.getMedication());
        assertEquals(0.0, log.getAmount());
        assertNull(log.getNotes());
        assertNull(log.getUser());
    }

    @Test
    void testParameterizedConstructor() {
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        LocalDateTime takenAt = LocalDateTime.of(2025, 6, 15, 8, 30);
        MedicationLog log = new MedicationLog(medication, 500.0, takenAt);

        assertNotNull(log.getId());
        assertSame(medication, log.getMedication());
        assertEquals(500.0, log.getAmount());
        assertEquals(takenAt, log.getTakenAt());
        assertNotNull(log.getCreatedAt());
    }

    @Test
    void testSetId() {
        MedicationLog log = new MedicationLog();
        log.setId("custom-id");
        assertEquals("custom-id", log.getId());
    }

    @Test
    void testSetMedication() {
        MedicationLog log = new MedicationLog();
        Medication medication = new Medication("Ibuprofen", MedicationProvisionType.PILL);
        log.setMedication(medication);
        assertSame(medication, log.getMedication());
    }

    @Test
    void testSetAmount() {
        MedicationLog log = new MedicationLog();
        log.setAmount(250.0);
        assertEquals(250.0, log.getAmount());
    }

    @Test
    void testSetTakenAt() {
        MedicationLog log = new MedicationLog();
        LocalDateTime takenAt = LocalDateTime.of(2025, 3, 10, 14, 0);
        log.setTakenAt(takenAt);
        assertEquals(takenAt, log.getTakenAt());
    }

    @Test
    void testSetNotes() {
        MedicationLog log = new MedicationLog();
        log.setNotes("Taken with food");
        assertEquals("Taken with food", log.getNotes());
    }

    @Test
    void testSetCreatedAt() {
        MedicationLog log = new MedicationLog();
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 12, 0);
        log.setCreatedAt(createdAt);
        assertEquals(createdAt, log.getCreatedAt());
    }

    @Test
    void testSetUser() {
        MedicationLog log = new MedicationLog();
        User user = new User("test", "pass");
        log.setUser(user);
        assertSame(user, log.getUser());
    }

    @Test
    void testEqualsSameId() {
        MedicationLog l1 = new MedicationLog();
        MedicationLog l2 = new MedicationLog();
        l2.setId(l1.getId());
        assertEquals(l1, l2);
    }

    @Test
    void testEqualsDifferentId() {
        MedicationLog l1 = new MedicationLog();
        MedicationLog l2 = new MedicationLog();
        assertNotEquals(l1, l2);
    }

    @Test
    void testEqualsNull() {
        MedicationLog l = new MedicationLog();
        assertNotEquals(null, l);
    }

    @Test
    void testEqualsSameObject() {
        MedicationLog l = new MedicationLog();
        assertEquals(l, l);
    }

    @Test
    void testEqualsDifferentClass() {
        MedicationLog l = new MedicationLog();
        assertNotEquals("not a log", l);
    }

    @Test
    void testHashCodeConsistent() {
        MedicationLog l1 = new MedicationLog();
        MedicationLog l2 = new MedicationLog();
        l2.setId(l1.getId());
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    void testHashCodeDifferentForDifferentIds() {
        MedicationLog l1 = new MedicationLog();
        MedicationLog l2 = new MedicationLog();
        assertNotEquals(l1.hashCode(), l2.hashCode());
    }
}
