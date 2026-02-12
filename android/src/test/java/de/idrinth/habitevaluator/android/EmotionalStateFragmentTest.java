package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;

import static org.junit.Assert.*;

public class EmotionalStateFragmentTest {

    @Test
    public void testGroupEntriesByPairIdWithEmptyLists() {
        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                new ArrayList<>(), new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGroupEntriesByPairIdWithPairsButNoEntries() {
        List<EmotionPair> pairs = new ArrayList<>();
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pairs.add(pair);

        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                pairs, new ArrayList<>());

        assertEquals(1, result.size());
        assertTrue(result.containsKey(pair.getId()));
        assertTrue(result.get(pair.getId()).isEmpty());
    }

    @Test
    public void testGroupEntriesByPairIdWithMatchingEntries() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        List<EmotionPair> pairs = new ArrayList<>();
        pairs.add(pair);

        EmotionEntry entry1 = new EmotionEntry(pair, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null);
        EmotionEntry entry2 = new EmotionEntry(pair, -3, LocalDateTime.of(2025, 1, 2, 10, 0), null);
        List<EmotionEntry> entries = new ArrayList<>();
        entries.add(entry1);
        entries.add(entry2);

        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                pairs, entries);

        assertEquals(1, result.size());
        assertEquals(2, result.get(pair.getId()).size());
    }

    @Test
    public void testGroupEntriesByPairIdSortsEntriesNewestFirst() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        List<EmotionPair> pairs = new ArrayList<>();
        pairs.add(pair);

        LocalDateTime older = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2025, 1, 2, 10, 0);
        EmotionEntry olderEntry = new EmotionEntry(pair, 5, older, null);
        EmotionEntry newerEntry = new EmotionEntry(pair, -3, newer, null);
        List<EmotionEntry> entries = new ArrayList<>();
        entries.add(olderEntry);
        entries.add(newerEntry);

        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                pairs, entries);

        List<EmotionEntry> sorted = result.get(pair.getId());
        assertEquals(newerEntry, sorted.get(0));
        assertEquals(olderEntry, sorted.get(1));
    }

    @Test
    public void testGroupEntriesByPairIdWithMultiplePairs() {
        EmotionPair pair1 = new EmotionPair("Sad", "Happy");
        EmotionPair pair2 = new EmotionPair("Anxious", "Calm");
        List<EmotionPair> pairs = new ArrayList<>();
        pairs.add(pair1);
        pairs.add(pair2);

        EmotionEntry entry1 = new EmotionEntry(pair1, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null);
        EmotionEntry entry2 = new EmotionEntry(pair2, -3, LocalDateTime.of(2025, 1, 2, 10, 0), null);
        List<EmotionEntry> entries = new ArrayList<>();
        entries.add(entry1);
        entries.add(entry2);

        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                pairs, entries);

        assertEquals(2, result.size());
        assertEquals(1, result.get(pair1.getId()).size());
        assertEquals(1, result.get(pair2.getId()).size());
    }

    @Test
    public void testGroupEntriesByPairIdIgnoresEntriesWithNullPair() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        List<EmotionPair> pairs = new ArrayList<>();
        pairs.add(pair);

        EmotionEntry entryWithPair = new EmotionEntry(pair, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null);
        EmotionEntry entryWithNullPair = new EmotionEntry(null, 3, LocalDateTime.of(2025, 1, 2, 10, 0), null);
        List<EmotionEntry> entries = new ArrayList<>();
        entries.add(entryWithPair);
        entries.add(entryWithNullPair);

        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                pairs, entries);

        assertEquals(1, result.get(pair.getId()).size());
    }

    @Test
    public void testGroupEntriesByPairIdDoesNotIncludeUnmatchedEntries() {
        EmotionPair pair1 = new EmotionPair("Sad", "Happy");
        EmotionPair pair2 = new EmotionPair("Anxious", "Calm");
        List<EmotionPair> pairs = new ArrayList<>();
        pairs.add(pair1);

        EmotionEntry entry = new EmotionEntry(pair2, 5, LocalDateTime.of(2025, 1, 1, 10, 0), null);
        List<EmotionEntry> entries = new ArrayList<>();
        entries.add(entry);

        Map<String, List<EmotionEntry>> result = EmotionalStateFragment.groupEntriesByPairId(
                pairs, entries);

        assertEquals(1, result.size());
        assertTrue(result.get(pair1.getId()).isEmpty());
    }
}
