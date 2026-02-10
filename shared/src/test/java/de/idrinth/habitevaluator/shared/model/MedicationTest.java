package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicationTest {

    @Test
    void testDefaultConstructor() {
        Medication medication = new Medication();
        assertNotNull(medication.getId());
        assertEquals(36, medication.getId().length());
        assertNull(medication.getName());
        assertNull(medication.getProvisionType());
        assertNull(medication.getWikipediaLink());
        assertNull(medication.getUser());
    }

    @Test
    void testParameterizedConstructor() {
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        assertNotNull(medication.getId());
        assertEquals("Aspirin", medication.getName());
        assertEquals(MedicationProvisionType.PILL, medication.getProvisionType());
    }

    @Test
    void testSetId() {
        Medication medication = new Medication();
        medication.setId("custom-id");
        assertEquals("custom-id", medication.getId());
    }

    @Test
    void testSetName() {
        Medication medication = new Medication();
        medication.setName("Ibuprofen");
        assertEquals("Ibuprofen", medication.getName());
    }

    @Test
    void testSetWikipediaLink() {
        Medication medication = new Medication();
        medication.setWikipediaLink("https://en.wikipedia.org/wiki/Aspirin");
        assertEquals("https://en.wikipedia.org/wiki/Aspirin", medication.getWikipediaLink());
    }

    @Test
    void testSetProvisionType() {
        Medication medication = new Medication();
        medication.setProvisionType(MedicationProvisionType.LIQUID_ML);
        assertEquals(MedicationProvisionType.LIQUID_ML, medication.getProvisionType());
    }

    @Test
    void testSetUser() {
        Medication medication = new Medication();
        User user = new User("test", "pass");
        medication.setUser(user);
        assertSame(user, medication.getUser());
    }

    @Test
    void testGetUnitPill() {
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        assertEquals("mg", medication.getUnit());
    }

    @Test
    void testGetUnitLiquidDrops() {
        Medication medication = new Medication("Vitamin D", MedicationProvisionType.LIQUID_DROPS);
        assertEquals("drops", medication.getUnit());
    }

    @Test
    void testGetUnitLiquidMl() {
        Medication medication = new Medication("Cough Syrup", MedicationProvisionType.LIQUID_ML);
        assertEquals("ml", medication.getUnit());
    }

    @Test
    void testGetUnitNullProvisionType() {
        Medication medication = new Medication();
        assertEquals("", medication.getUnit());
    }

    @Test
    void testToString() {
        Medication medication = new Medication("Aspirin", MedicationProvisionType.PILL);
        assertEquals("Aspirin (mg)", medication.toString());
    }

    @Test
    void testToStringLiquidDrops() {
        Medication medication = new Medication("Vitamin D", MedicationProvisionType.LIQUID_DROPS);
        assertEquals("Vitamin D (drops)", medication.toString());
    }

    @Test
    void testEqualsSameId() {
        Medication m1 = new Medication();
        Medication m2 = new Medication();
        m2.setId(m1.getId());
        assertEquals(m1, m2);
    }

    @Test
    void testEqualsDifferentId() {
        Medication m1 = new Medication();
        Medication m2 = new Medication();
        assertNotEquals(m1, m2);
    }

    @Test
    void testEqualsNull() {
        Medication m = new Medication();
        assertNotEquals(null, m);
    }

    @Test
    void testEqualsSameObject() {
        Medication m = new Medication();
        assertEquals(m, m);
    }

    @Test
    void testEqualsDifferentClass() {
        Medication m = new Medication();
        assertNotEquals("not a medication", m);
    }

    @Test
    void testHashCodeConsistent() {
        Medication m1 = new Medication();
        Medication m2 = new Medication();
        m2.setId(m1.getId());
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void testHashCodeDifferentForDifferentIds() {
        Medication m1 = new Medication();
        Medication m2 = new Medication();
        assertNotEquals(m1.hashCode(), m2.hashCode());
    }
}
