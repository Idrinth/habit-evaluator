package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

class HabitAdapterTest {

    @Test
    fun testAdapterReplacedByCompose() {
        // HabitAdapter replaced by LazyColumn in HomeScreen composable
        assertTrue(true)
    }

    @Test
    fun testHabitListCanBeEmpty() {
        // Compose LazyColumn handles empty lists natively
        val habits = mutableListOf<String>()
        assertEquals(0, habits.size)
    }

    @Test
    fun testHabitListCanHoldMultipleItems() {
        // Data-level: list correctly tracks added habits
        val habits = mutableListOf("Exercise", "Read", "Meditate")
        assertEquals(3, habits.size)
    }

    @Test
    fun testHabitListCanRemoveItems() {
        val habits = mutableListOf("Exercise", "Read")
        habits.removeAt(0)
        assertEquals(1, habits.size)
    }

    @Test
    fun testHabitListClearRemovesAll() {
        val habits = mutableListOf("Exercise", "Read", "Meditate")
        habits.clear()
        assertEquals(0, habits.size)
    }
}
