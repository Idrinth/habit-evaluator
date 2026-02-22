package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.MedicationProvisionType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for provision type mapping logic equivalent to what MedicationListFragment provided.
 * Maps a spinner/dropdown position (0-based) to a MedicationProvisionType enum value,
 * returning null for out-of-range positions.
 */
class MedicationListScreenTest {

    /**
     * Replication of mapPositionToProvisionType logic from MedicationListFragment.
     */
    private fun mapPositionToProvisionType(position: Int): MedicationProvisionType? {
        val values = MedicationProvisionType.values()
        return if (position < 0 || position >= values.size) null else values[position]
    }

    @Test
    fun testMapPositionToProvisionTypePill() {
        assertEquals(MedicationProvisionType.PILL, mapPositionToProvisionType(0))
    }

    @Test
    fun testMapPositionToProvisionTypeLiquidDrops() {
        assertEquals(MedicationProvisionType.LIQUID_DROPS, mapPositionToProvisionType(1))
    }

    @Test
    fun testMapPositionToProvisionTypeLiquidMl() {
        assertEquals(MedicationProvisionType.LIQUID_ML, mapPositionToProvisionType(2))
    }

    @Test
    fun testMapPositionToProvisionTypeNegativeReturnsNull() {
        assertNull(mapPositionToProvisionType(-1))
    }

    @Test
    fun testMapPositionToProvisionTypeOutOfRangeReturnsNull() {
        assertNull(mapPositionToProvisionType(99))
    }

    @Test
    fun testMapPositionToProvisionTypeExactBoundary() {
        assertNull(mapPositionToProvisionType(MedicationProvisionType.values().size))
    }

    @Test
    fun testAllProvisionTypesAreMapped() {
        for (i in MedicationProvisionType.values().indices) {
            assertNotNull(mapPositionToProvisionType(i))
        }
    }
}
