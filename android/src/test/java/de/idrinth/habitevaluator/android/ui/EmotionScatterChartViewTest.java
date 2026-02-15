package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    @Test
    void testSetDataWithNullPairs() {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        view.setData(null);
    }

    @Test
    void testSetDataWithEmptyPairs() {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        view.setData(new ArrayList<>());
    }

    @Test
    void testSetDataGeneratesHourLabels() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        pairs.add(new EmotionScatterChartView.ScatterPair("Test", new ArrayList<>()));
        view.setData(pairs);

        Field labelsField = EmotionScatterChartView.class.getDeclaredField("labels");
        labelsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) labelsField.get(view);
        assertEquals(5, labels.size());
        assertEquals("00:00", labels.get(0));
        assertEquals("06:00", labels.get(1));
        assertEquals("12:00", labels.get(2));
        assertEquals("18:00", labels.get(3));
        assertEquals("24:00", labels.get(4));
    }

    @Test
    void testSetDataStoresPairs() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        pairs.add(new EmotionScatterChartView.ScatterPair("Pair A", new ArrayList<>()));
        pairs.add(new EmotionScatterChartView.ScatterPair("Pair B", new ArrayList<>()));
        view.setData(pairs);

        Field pairsField = EmotionScatterChartView.class.getDeclaredField("pairs");
        pairsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<EmotionScatterChartView.ScatterPair> stored =
                (List<EmotionScatterChartView.ScatterPair>) pairsField.get(view);
        assertEquals(2, stored.size());
        assertEquals("Pair A", stored.get(0).pairLabel);
        assertEquals("Pair B", stored.get(1).pairLabel);
    }

    @Test
    void testGetLegendHeightWithNoPairs() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        view.setData(new ArrayList<>());

        Method method = EmotionScatterChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        assertEquals(0f, height, 0.001f);
    }

    @Test
    void testGetLegendHeightWithOnePair() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        pairs.add(new EmotionScatterChartView.ScatterPair("P1", new ArrayList<>()));
        view.setData(pairs);

        Method method = EmotionScatterChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        assertEquals(40f, height, 0.001f);
    }

    @Test
    void testGetLegendHeightWithFourPairs() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            pairs.add(new EmotionScatterChartView.ScatterPair("P" + i, new ArrayList<>()));
        }
        view.setData(pairs);

        Method method = EmotionScatterChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        assertEquals(64f, height, 0.001f);
    }

    @Test
    void testConstructorWithTwoArgs() {
        EmotionScatterChartView view = new EmotionScatterChartView(null, null);
        assertNotNull(view);
    }

    @Test
    void testConstructorWithThreeArgs() {
        EmotionScatterChartView view = new EmotionScatterChartView(null, null, 0);
        assertNotNull(view);
    }

    @Test
    void testSetDataReplacesPreviousPairs() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> first = new ArrayList<>();
        first.add(new EmotionScatterChartView.ScatterPair("Old", new ArrayList<>()));
        view.setData(first);

        List<EmotionScatterChartView.ScatterPair> second = new ArrayList<>();
        second.add(new EmotionScatterChartView.ScatterPair("New1", new ArrayList<>()));
        second.add(new EmotionScatterChartView.ScatterPair("New2", new ArrayList<>()));
        view.setData(second);

        Field pairsField = EmotionScatterChartView.class.getDeclaredField("pairs");
        pairsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<EmotionScatterChartView.ScatterPair> stored =
                (List<EmotionScatterChartView.ScatterPair>) pairsField.get(view);
        assertEquals(2, stored.size());
        assertEquals("New1", stored.get(0).pairLabel);
    }

    @Test
    void testGetLegendHeightWithThreePairs() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            pairs.add(new EmotionScatterChartView.ScatterPair("P" + i, new ArrayList<>()));
        }
        view.setData(pairs);

        Method method = EmotionScatterChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 3 pairs: (3 + 2) / 3 = 1 row => 1 * 24 + 16 = 40
        assertEquals(40f, height, 0.001f);
    }

    @Test
    void testGetLegendHeightWithTenPairs() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pairs.add(new EmotionScatterChartView.ScatterPair("P" + i, new ArrayList<>()));
        }
        view.setData(pairs);

        Method method = EmotionScatterChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 10 pairs: (10 + 2) / 3 = 4 rows => 4 * 24 + 16 = 112
        assertEquals(112f, height, 0.001f);
    }

    @Test
    void testScatterEntryWithFractionalHour() {
        EmotionScatterChartView.ScatterEntry entry =
                new EmotionScatterChartView.ScatterEntry(10.75f, 3.5f);
        assertEquals(10.75f, entry.hourOfDay, 0.001f);
        assertEquals(3.5f, entry.strength, 0.001f);
    }

    @Test
    void testScatterPairWithManyEntries() {
        List<EmotionScatterChartView.ScatterEntry> entries = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            entries.add(new EmotionScatterChartView.ScatterEntry(
                    i % 24, (i % 21) - 10));
        }
        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("Many entries", entries);
        assertEquals(100, pair.entries.size());
    }

    @Test
    void testScatterPairWithEmptyLabel() {
        EmotionScatterChartView.ScatterPair pair =
                new EmotionScatterChartView.ScatterPair("", new ArrayList<>());
        assertEquals("", pair.pairLabel);
    }

    @Test
    void testSetDataNullDefaultsToEmptyPairsList() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        view.setData(null);

        Field pairsField = EmotionScatterChartView.class.getDeclaredField("pairs");
        pairsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<EmotionScatterChartView.ScatterPair> stored =
                (List<EmotionScatterChartView.ScatterPair>) pairsField.get(view);
        assertNotNull(stored);
        assertTrue(stored.isEmpty());
    }

    @Test
    void testPairColorsAreAllOpaque() throws Exception {
        Field field = EmotionScatterChartView.class.getDeclaredField("PAIR_COLORS");
        field.setAccessible(true);
        int[] colors = (int[]) field.get(null);
        for (int color : colors) {
            int alpha = (color >> 24) & 0xFF;
            assertEquals(0xFF, alpha, "Each color should be fully opaque");
        }
    }

    @Test
    void testPairColorsHasTenEntries() throws Exception {
        Field field = EmotionScatterChartView.class.getDeclaredField("PAIR_COLORS");
        field.setAccessible(true);
        int[] colors = (int[]) field.get(null);
        assertEquals(10, colors.length);
    }

    @Test
    void testPairColorsAreAllUnique() throws Exception {
        Field field = EmotionScatterChartView.class.getDeclaredField("PAIR_COLORS");
        field.setAccessible(true);
        int[] colors = (int[]) field.get(null);
        for (int i = 0; i < colors.length; i++) {
            for (int j = i + 1; j < colors.length; j++) {
                assertNotEquals(colors[i], colors[j],
                        "Colors at index " + i + " and " + j + " should be unique");
            }
        }
    }

    @Test
    void testSetDataAlwaysGeneratesFiveLabels() throws Exception {
        EmotionScatterChartView view = new EmotionScatterChartView(null);
        List<EmotionScatterChartView.ScatterPair> pairs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pairs.add(new EmotionScatterChartView.ScatterPair("P" + i, new ArrayList<>()));
        }
        view.setData(pairs);

        Field labelsField = EmotionScatterChartView.class.getDeclaredField("labels");
        labelsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) labelsField.get(view);
        assertEquals(5, labels.size());
    }

    @Test
    void testPaddingConstantsArePositive() throws Exception {
        assertFieldPositive("PADDING_LEFT");
        assertFieldPositive("PADDING_RIGHT");
        assertFieldPositive("PADDING_TOP");
        assertFieldPositive("PADDING_BOTTOM");
    }

    private void assertFieldPositive(String fieldName) throws Exception {
        Field field = EmotionScatterChartView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        float value = field.getFloat(null);
        assertTrue(value > 0, fieldName + " should be positive");
    }
}
