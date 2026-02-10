package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicationProvisionTypeTest {

    @Test
    void testEnumValues() {
        MedicationProvisionType[] values = MedicationProvisionType.values();
        assertEquals(3, values.length);
    }

    @Test
    void testPillValue() {
        assertEquals(MedicationProvisionType.PILL, MedicationProvisionType.valueOf("PILL"));
    }

    @Test
    void testLiquidDropsValue() {
        assertEquals(MedicationProvisionType.LIQUID_DROPS, MedicationProvisionType.valueOf("LIQUID_DROPS"));
    }

    @Test
    void testLiquidMlValue() {
        assertEquals(MedicationProvisionType.LIQUID_ML, MedicationProvisionType.valueOf("LIQUID_ML"));
    }

    @Test
    void testInvalidValueThrows() {
        assertThrows(IllegalArgumentException.class, () -> MedicationProvisionType.valueOf("INVALID"));
    }
}
