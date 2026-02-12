package de.idrinth.habitevaluator.android;

import org.junit.Test;

import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;

import static org.junit.Assert.*;

public class MedicationListFragmentTest {

    @Test
    public void testMapPositionToProvisionTypePill() {
        assertEquals(MedicationProvisionType.PILL,
                MedicationListFragment.mapPositionToProvisionType(0));
    }

    @Test
    public void testMapPositionToProvisionTypeLiquidDrops() {
        assertEquals(MedicationProvisionType.LIQUID_DROPS,
                MedicationListFragment.mapPositionToProvisionType(1));
    }

    @Test
    public void testMapPositionToProvisionTypeLiquidMl() {
        assertEquals(MedicationProvisionType.LIQUID_ML,
                MedicationListFragment.mapPositionToProvisionType(2));
    }

    @Test
    public void testMapPositionToProvisionTypeNegativeReturnsNull() {
        assertNull(MedicationListFragment.mapPositionToProvisionType(-1));
    }

    @Test
    public void testMapPositionToProvisionTypeOutOfRangeReturnsNull() {
        assertNull(MedicationListFragment.mapPositionToProvisionType(99));
    }

    @Test
    public void testMapPositionToProvisionTypeExactBoundary() {
        assertNull(MedicationListFragment.mapPositionToProvisionType(
                MedicationProvisionType.values().length));
    }

    @Test
    public void testAllProvisionTypesAreMapped() {
        for (int i = 0; i < MedicationProvisionType.values().length; i++) {
            assertNotNull(MedicationListFragment.mapPositionToProvisionType(i));
        }
    }
}
