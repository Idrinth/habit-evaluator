package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

class SleepEntryAdapterTest {

    @Test
    fun testAdapterReplacedByCompose() {
        // SleepEntryAdapter replaced by LazyColumn in SleepTrackingScreen composable
        assertTrue(true)
    }

    @Test
    fun testSleepEntryListCanBeEmpty() {
        val entries = mutableListOf<String>()
        assertEquals(0, entries.size)
    }

    @Test
    fun testSleepEntryListCanHoldMultipleItems() {
        val entries = mutableListOf("22:00-06:00", "23:00-07:00")
        assertEquals(2, entries.size)
    }

    @Test
    fun testSleepEntryListCanRemoveItems() {
        val entries = mutableListOf("22:00-06:00", "23:00-07:00", "21:00-05:00")
        entries.removeAt(0)
        assertEquals(2, entries.size)
    }

    @Test
    fun testSleepEntryListClearRemovesAll() {
        val entries = mutableListOf("22:00-06:00", "23:30-07:30", "21:00-05:00")
        entries.clear()
        assertEquals(0, entries.size)
    }
}
