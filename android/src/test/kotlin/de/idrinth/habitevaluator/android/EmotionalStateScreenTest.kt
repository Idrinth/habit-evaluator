package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.model.EmotionPair
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

/**
 * Tests for emotion entry grouping logic equivalent to what EmotionalStateFragment provided.
 * Groups emotion entries by their pair ID and sorts each group newest-first.
 */
class EmotionalStateScreenTest {

    /**
     * Replication of groupEntriesByPairId logic from EmotionalStateFragment.
     * Creates a map keyed by EmotionPair ID, with entries sorted newest-first.
     * Only entries belonging to one of the provided pairs are included.
     */
    private fun groupEntriesByPairId(
        pairs: List<EmotionPair>,
        entries: List<EmotionEntry>
    ): Map<String, List<EmotionEntry>> {
        val result = mutableMapOf<String, MutableList<EmotionEntry>>()
        for (pair in pairs) {
            result[pair.id] = mutableListOf()
        }
        for (entry in entries) {
            val pairId = entry.emotionPair?.id ?: continue
            result[pairId]?.add(entry)
        }
        for (list in result.values) {
            list.sortByDescending { it.recordedAt }
        }
        return result
    }

    @Test
    fun testGroupEntriesByPairIdWithEmptyLists() {
        val result = groupEntriesByPairId(emptyList(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun testGroupEntriesByPairIdWithPairsButNoEntries() {
        val pair = EmotionPair("Sad", "Happy")
        val result = groupEntriesByPairId(listOf(pair), emptyList())

        assertEquals(1, result.size)
        assertTrue(result.containsKey(pair.id))
        assertTrue(result[pair.id]!!.isEmpty())
    }

    @Test
    fun testGroupEntriesByPairIdWithMatchingEntries() {
        val pair = EmotionPair("Sad", "Happy")
        val entry1 = EmotionEntry(pair, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null)
        val entry2 = EmotionEntry(pair, -3, LocalDateTime.of(2025, 1, 2, 10, 0), null)

        val result = groupEntriesByPairId(listOf(pair), listOf(entry1, entry2))

        assertEquals(1, result.size)
        assertEquals(2, result[pair.id]!!.size)
    }

    @Test
    fun testGroupEntriesByPairIdSortsEntriesNewestFirst() {
        val pair = EmotionPair("Sad", "Happy")
        val older = LocalDateTime.of(2025, 1, 1, 10, 0)
        val newer = LocalDateTime.of(2025, 1, 2, 10, 0)
        val olderEntry = EmotionEntry(pair, 5, older, null)
        val newerEntry = EmotionEntry(pair, -3, newer, null)

        val result = groupEntriesByPairId(listOf(pair), listOf(olderEntry, newerEntry))

        val sorted = result[pair.id]!!
        assertEquals(newerEntry, sorted[0])
        assertEquals(olderEntry, sorted[1])
    }

    @Test
    fun testGroupEntriesByPairIdWithMultiplePairs() {
        val pair1 = EmotionPair("Sad", "Happy")
        val pair2 = EmotionPair("Anxious", "Calm")
        val entry1 = EmotionEntry(pair1, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null)
        val entry2 = EmotionEntry(pair2, -3, LocalDateTime.of(2025, 1, 2, 10, 0), null)

        val result = groupEntriesByPairId(listOf(pair1, pair2), listOf(entry1, entry2))

        assertEquals(2, result.size)
        assertEquals(1, result[pair1.id]!!.size)
        assertEquals(1, result[pair2.id]!!.size)
    }

    @Test
    fun testGroupEntriesByPairIdIgnoresEntriesWithNullPair() {
        val pair = EmotionPair("Sad", "Happy")
        val entryWithPair = EmotionEntry(pair, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null)
        val entryWithNullPair = EmotionEntry(null, 3, LocalDateTime.of(2025, 1, 2, 10, 0), null)

        val result = groupEntriesByPairId(listOf(pair), listOf(entryWithPair, entryWithNullPair))

        assertEquals(1, result[pair.id]!!.size)
    }

    @Test
    fun testGroupEntriesByPairIdDoesNotIncludeUnmatchedEntries() {
        val pair1 = EmotionPair("Sad", "Happy")
        val pair2 = EmotionPair("Anxious", "Calm")
        // Only pair1 is in the list, but entry belongs to pair2
        val entry = EmotionEntry(pair2, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null)

        val result = groupEntriesByPairId(listOf(pair1), listOf(entry))

        assertEquals(1, result.size)
        assertTrue(result[pair1.id]!!.isEmpty())
    }

    @Test
    fun testExpandedStateDefaultsToCollapsed() {
        val expandedPairs = mutableMapOf<String, Boolean>()
        val pair = EmotionPair("Sad", "Happy")

        assertFalse(expandedPairs[pair.id] == true)
    }

    @Test
    fun testExpandedStateTogglesFromCollapsedToExpanded() {
        val expandedPairs = mutableMapOf<String, Boolean>()
        val pair = EmotionPair("Sad", "Happy")

        expandedPairs[pair.id] = !(expandedPairs[pair.id] == true)

        assertTrue(expandedPairs[pair.id] == true)
    }

    @Test
    fun testExpandedStateTogglesFromExpandedToCollapsed() {
        val expandedPairs = mutableMapOf<String, Boolean>()
        val pair = EmotionPair("Sad", "Happy")
        expandedPairs[pair.id] = true

        expandedPairs[pair.id] = !(expandedPairs[pair.id] == true)

        assertFalse(expandedPairs[pair.id] == true)
    }

    @Test
    fun testExpandedStatesAreIndependentPerPair() {
        val expandedPairs = mutableMapOf<String, Boolean>()
        val pair1 = EmotionPair("Sad", "Happy")
        val pair2 = EmotionPair("Anxious", "Calm")

        expandedPairs[pair1.id] = true

        assertTrue(expandedPairs[pair1.id] == true)
        assertFalse(expandedPairs[pair2.id] == true)
    }

    @Test
    fun testPairWithNoEntriesHasNoExpandableContent() {
        val pair = EmotionPair("Sad", "Happy")
        val result = groupEntriesByPairId(listOf(pair), emptyList())

        val pairEntries = result[pair.id]!!
        assertTrue(pairEntries.isEmpty())
    }

    @Test
    fun testPairWithEntriesHasExpandableContent() {
        val pair = EmotionPair("Sad", "Happy")
        val entry = EmotionEntry(pair, 5, LocalDateTime.of(2025, 1, 1, 10, 0), "feeling good")

        val result = groupEntriesByPairId(listOf(pair), listOf(entry))

        val pairEntries = result[pair.id]!!
        assertTrue(pairEntries.isNotEmpty())
        assertEquals("feeling good", pairEntries[0].notes)
        assertEquals(5, pairEntries[0].strength)
    }
}
