package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmotionScatterChartViewTest {

    @Test
    void testScatterEntryStoresHourOfDay() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(14.5f, 7.0f);
        assertEquals(14.5f, entry.hourOfDay, 0.001f);
    }

    @Test
    void testScatterEntryStoresStrength() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(14.5f, 7.0f);
        assertEquals(7.0f, entry.strength, 0.001f);
    }

    @Test
    void testScatterEntryWithZeroValues() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(0f, 0f);
        assertEquals(0f, entry.hourOfDay, 0.001f);
        assertEquals(0f, entry.strength, 0.001f);
    }

    @Test
    void testScatterEntryWithNegativeStrength() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(8.0f, -5.5f);
        assertEquals(-5.5f, entry.strength, 0.001f);
    }

    @Test
    void testScatterEntryWithBoundaryHours() {
        EmotionScatterChartView.ScatterEntry earlyEntry =
                new EmotionScatterChartView.ScatterEntry(0f, 3.0f);
        EmotionScatterChartView.ScatterEntry lateEntry =
                new EmotionScatterChartView.ScatterEntry(24f, -3.0f);
        assertEquals(0f, earlyEntry.hourOfDay, 0.001f);
        assertEquals(24f, lateEntry.hourOfDay, 0.001f);
    }

    @Test
    void testScatterEntryWithMaxStrength() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(12f, 10f);
        assertEquals(10f, entry.strength, 0.001f);
    }

    @Test
    void testScatterEntryWithMinStrength() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(12f, -10f);
        assertEquals(-10f, entry.strength, 0.001f);
    }

    @Test
    void testScatterPairStoresLabel() {
        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("Happy - Sad", new ArrayList<>());
        assertEquals("Happy - Sad", pair.pairLabel);
    }

    @Test
    void testScatterPairStoresEntries() {
        List<EmotionScatterChartView.ScatterEntry> entries = new ArrayList<>();
        entries.add(new EmotionScatterChartView.ScatterEntry(10f, 5f));
        entries.add(new EmotionScatterChartView.ScatterEntry(14f, -2f));

        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("Calm - Anxious", entries);

        assertEquals(2, pair.entries.size());
        assertEquals(10f, pair.entries.get(0).hourOfDay, 0.001f);
        assertEquals(5f, pair.entries.get(0).strength, 0.001f);
    }

    @Test
    void testScatterPairWithNullEntries() {
        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("Test", null);
        assertNotNull(pair.entries);
        assertTrue(pair.entries.isEmpty());
    }

    @Test
    void testScatterPairWithNullLabel() {
        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair(null, new ArrayList<>());
        assertNull(pair.pairLabel);
    }

    @Test
    void testScatterPairWithEmptyEntries() {
        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("Label", new ArrayList<>());
        assertTrue(pair.entries.isEmpty());
    }

    @Test
    void testScatterPairEntriesListIsNotCopied() {
        List<EmotionScatterChartView.ScatterEntry> entries = new ArrayList<>();
        entries.add(new EmotionScatterChartView.ScatterEntry(8f, 3f));

        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("Test", entries);

        assertEquals(1, pair.entries.size());

        entries.add(new EmotionScatterChartView.ScatterEntry(12f, -1f));
        assertEquals(2, pair.entries.size());
    }

    @Test
    void testMultipleScatterPairsAreIndependent() {
        List<EmotionScatterChartView.ScatterEntry> entries1 = new ArrayList<>();
        entries1.add(new EmotionScatterChartView.ScatterEntry(8f, 5f));

        List<EmotionScatterChartView.ScatterEntry> entries2 = new ArrayList<>();
        entries2.add(new EmotionScatterChartView.ScatterEntry(10f, -3f));
        entries2.add(new EmotionScatterChartView.ScatterEntry(14f, 2f));

        EmotionScatterChartView.ScatterPair pair1 =
                new EmotionScatterChartView.ScatterPair("Pair 1", entries1);
        EmotionScatterChartView.ScatterPair pair2 =
                new EmotionScatterChartView.ScatterPair("Pair 2", entries2);

        assertEquals(1, pair1.entries.size());
        assertEquals(2, pair2.entries.size());
    }
}
