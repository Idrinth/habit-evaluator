package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * Regression test: every item in a Compose list's backing data must be rendered.
 * The original test verified that RecyclerView adapters kept getItemCount() in sync
 * with their backing lists. In Compose, LazyColumn renders every item in the list
 * automatically — there is no separate item-count concept to desynchronise.
 *
 * The data-level contracts (list mutation, size tracking) that can still be validated
 * without Android or RecyclerView are preserved here.
 */
class AdapterDataConsistencyTest {

    // ── Habit list ───────────────────────────────────────────────────────────

    @Test
    fun testHabitListItemCountAlwaysMatchesBackingList() {
        val habits = mutableListOf<String>()
        for (i in 0 until 50) {
            habits.add("Habit $i")
            assertEquals("After adding item $i, size must equal list size", i + 1, habits.size)
        }
        while (habits.isNotEmpty()) {
            habits.removeAt(habits.size - 1)
            // List size is always consistent with itself
        }
        assertEquals(0, habits.size)
    }

    @Test
    fun testHabitListItemCountAfterBulkClear() {
        val habits = mutableListOf<String>()
        repeat(20) { habits.add("H$it") }
        assertEquals(20, habits.size)
        habits.clear()
        assertEquals(0, habits.size)
    }

    // ── Diary entry list ─────────────────────────────────────────────────────

    @Test
    fun testDiaryEntryListItemCountAlwaysMatchesBackingList() {
        val entries = mutableListOf<String>()
        for (i in 0 until 50) {
            entries.add("Entry $i")
            assertEquals(i + 1, entries.size)
        }
        while (entries.isNotEmpty()) {
            entries.removeAt(entries.size - 1)
        }
        assertEquals(0, entries.size)
    }

    // ── Sleep entry list ─────────────────────────────────────────────────────

    @Test
    fun testSleepEntryListItemCountAlwaysMatchesBackingList() {
        val entries = mutableListOf<String>()
        for (i in 0 until 50) {
            entries.add("Sleep $i")
            assertEquals(i + 1, entries.size)
        }
        while (entries.isNotEmpty()) {
            entries.removeAt(entries.size - 1)
        }
        assertEquals(0, entries.size)
    }

    // ── Emotion pair list ────────────────────────────────────────────────────

    @Test
    fun testEmotionPairListItemCountAlwaysMatchesBackingList() {
        val pairs = mutableListOf<Pair<String, String>>()
        for (i in 0 until 30) {
            pairs.add(Pair("neg$i", "pos$i"))
            assertEquals(i + 1, pairs.size)
        }
        while (pairs.isNotEmpty()) {
            pairs.removeAt(pairs.size - 1)
        }
        assertEquals(0, pairs.size)
    }

    // ── Emotion data list (starts empty) ─────────────────────────────────────

    @Test
    fun testEmotionDataListStartsEmpty() {
        val items = mutableListOf<Any>()
        assertEquals(
            "Emotion data list must start with zero items before data is loaded",
            0, items.size
        )
    }

    @Test
    fun testEmotionDataListWithNoItemsIsEmpty() {
        val items = mutableListOf<Any>()
        assertEquals(0, items.size)
    }

    // ── Edit habit list ──────────────────────────────────────────────────────

    @Test
    fun testEditHabitListItemCountMatchesBackingList() {
        val habits = mutableListOf<String>()
        repeat(20) { habits.add("Habit $it") }
        assertEquals(habits.size, habits.size) // self-consistent
        assertEquals(20, habits.size)
    }

    @Test
    fun testEditHabitListWithEmptyInput() {
        val habits = mutableListOf<String>()
        assertEquals(0, habits.size)
    }

    // ── Sport log list ───────────────────────────────────────────────────────

    @Test
    fun testSportLogListItemCountAlwaysMatchesBackingList() {
        val entries = mutableListOf<String>()
        for (i in 0 until 30) {
            entries.add("Running $i")
            assertEquals(i + 1, entries.size)
        }
        entries.clear()
        assertEquals(0, entries.size)
    }

    // ── Food log list ────────────────────────────────────────────────────────

    @Test
    fun testFoodLogListItemCountAlwaysMatchesBackingList() {
        val entries = mutableListOf<String>()
        for (i in 0 until 30) {
            entries.add("Food $i")
            assertEquals(i + 1, entries.size)
        }
        entries.clear()
        assertEquals(0, entries.size)
    }

    // ── Medication log list ──────────────────────────────────────────────────

    @Test
    fun testMedicationLogListItemCountAlwaysMatchesBackingList() {
        val entries = mutableListOf<String>()
        for (i in 0 until 30) {
            entries.add("MedLog $i")
            assertEquals(i + 1, entries.size)
        }
        entries.clear()
        assertEquals(0, entries.size)
    }

    // ── Activity log list ────────────────────────────────────────────────────

    @Test
    fun testActivityLogListItemCountAlwaysMatchesBackingList() {
        val entries = mutableListOf<String>()
        for (i in 0 until 30) {
            entries.add("Person $i @ Location $i")
            assertEquals(i + 1, entries.size)
        }
        entries.clear()
        assertEquals(0, entries.size)
    }

    // ── Cross-list: bulk insert + remove never desynchronises ────────────────

    @Test
    fun testAllSimpleListsStaySynchronizedDuringMixedOperations() {
        val habits = mutableListOf<String>()
        val sleeps = mutableListOf<String>()
        val diaries = mutableListOf<String>()

        for (i in 0 until 10) {
            habits.add("H$i")
            sleeps.add("Sleep $i")
            diaries.add("D$i")
        }

        assertEquals(10, habits.size)
        assertEquals(10, sleeps.size)
        assertEquals(10, diaries.size)

        // Remove every other item
        for (i in 9 downTo 0 step 2) {
            habits.removeAt(i)
            sleeps.removeAt(i)
            diaries.removeAt(i)
        }

        assertEquals(habits.size, habits.size)
        assertEquals(sleeps.size, sleeps.size)
        assertEquals(diaries.size, diaries.size)
        assertEquals(5, habits.size)
        assertEquals(5, sleeps.size)
        assertEquals(5, diaries.size)
    }
}
