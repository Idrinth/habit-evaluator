package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

import static org.junit.jupiter.api.Assertions.*;

class MedicationListFragmentTest {

    @Test
    void testMapPositionToProvisionTypePill() {
        assertEquals(MedicationProvisionType.PILL,
                MedicationListFragment.mapPositionToProvisionType(0));
    }

    @Test
    void testMapPositionToProvisionTypeLiquidDrops() {
        assertEquals(MedicationProvisionType.LIQUID_DROPS,
                MedicationListFragment.mapPositionToProvisionType(1));
    }

    @Test
    void testMapPositionToProvisionTypeLiquidMl() {
        assertEquals(MedicationProvisionType.LIQUID_ML,
                MedicationListFragment.mapPositionToProvisionType(2));
    }

    @Test
    void testMapPositionToProvisionTypeNegativeReturnsNull() {
        assertNull(MedicationListFragment.mapPositionToProvisionType(-1));
    }

    @Test
    void testMapPositionToProvisionTypeOutOfRangeReturnsNull() {
        assertNull(MedicationListFragment.mapPositionToProvisionType(99));
    }

    @Test
    void testMapPositionToProvisionTypeExactBoundary() {
        assertNull(MedicationListFragment.mapPositionToProvisionType(
                MedicationProvisionType.values().length));
    }

    @Test
    void testAllProvisionTypesAreMapped() {
        for (int i = 0; i < MedicationProvisionType.values().length; i++) {
            assertNotNull(MedicationListFragment.mapPositionToProvisionType(i));
        }
    }
}
