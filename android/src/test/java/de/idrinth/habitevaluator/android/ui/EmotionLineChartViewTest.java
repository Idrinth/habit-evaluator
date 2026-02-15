package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmotionLineChartViewTest {

    @Test
    void testPairColorsArrayIsNotEmpty() throws Exception {
        Field field = EmotionLineChartView.class.getDeclaredField("PAIR_COLORS");
        field.setAccessible(true);
        int[] colors = (int[]) field.get(null);
        assertTrue(colors.length > 0);
    }

    @Test
    void testPairColorsHasTenEntries() throws Exception {
        Field field = EmotionLineChartView.class.getDeclaredField("PAIR_COLORS");
        field.setAccessible(true);
        int[] colors = (int[]) field.get(null);
        assertEquals(10, colors.length);
    }

    @Test
    void testPairColorsAreAllOpaque() throws Exception {
        Field field = EmotionLineChartView.class.getDeclaredField("PAIR_COLORS");
        field.setAccessible(true);
        int[] colors = (int[]) field.get(null);
        for (int color : colors) {
            int alpha = (color >> 24) & 0xFF;
            assertEquals(0xFF, alpha, "Each color should be fully opaque");
        }
    }

    @Test
    void testPaddingConstantsArePositive() throws Exception {
        assertFieldPositive("PADDING_LEFT");
        assertFieldPositive("PADDING_RIGHT");
        assertFieldPositive("PADDING_TOP");
        assertFieldPositive("PADDING_BOTTOM");
    }

    private void assertFieldPositive(String fieldName) throws Exception {
        Field field = EmotionLineChartView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        float value = field.getFloat(null);
        assertTrue(value > 0, fieldName + " should be positive");
    }

    @Test
    void testSetDataWithNullLabelsDoesNotThrow() {
        // Use returnDefaultValues to construct the view
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(null, null, null);
    }

    @Test
    void testSetDataWithEmptyListsDoesNotThrow() {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void testSetDataWithValidDataDoesNotThrow() {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<String> labels = Arrays.asList("Mon", "Tue", "Wed");
        List<String> pairNames = Arrays.asList("Happy-Sad");
        List<List<Float>> values = new ArrayList<>();
        values.add(Arrays.asList(5.0f, -3.0f, 7.0f));
        view.setData(labels, pairNames, values);
    }

    @Test
    void testSetDataWithNullValuesInListsDoesNotThrow() {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<String> labels = Arrays.asList("Mon", "Tue", "Wed");
        List<String> pairNames = Arrays.asList("Pair 1");
        List<List<Float>> values = new ArrayList<>();
        values.add(Arrays.asList(5.0f, null, 7.0f));
        view.setData(labels, pairNames, values);
    }

    @Test
    void testSetDataReplacesPreviousData() {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(Arrays.asList("A"), Arrays.asList("P1"), new ArrayList<>());
        view.setData(Arrays.asList("B", "C"), Arrays.asList("P2"), new ArrayList<>());
        // Should not throw - data is replaced
    }

    @Test
    void testConstructorWithTwoArgsDoesNotThrow() {
        EmotionLineChartView view = new EmotionLineChartView(null, null);
        assertNotNull(view);
    }

    @Test
    void testConstructorWithThreeArgsDoesNotThrow() {
        EmotionLineChartView view = new EmotionLineChartView(null, null, 0);
        assertNotNull(view);
    }

    @Test
    void testGetLegendHeightWithNoPairs() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        assertEquals(0f, height, 0.001f, "Legend height should be 0 when no pairs");
    }

    @Test
    void testGetLegendHeightWithOnePair() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(Arrays.asList("Mon"), Arrays.asList("Happy-Sad"), new ArrayList<>());

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 1 pair: (1 + 2) / 3 = 1 row => 1 * 24 + 16 = 40
        assertEquals(40f, height, 0.001f);
    }

    @Test
    void testGetLegendHeightWithThreePairs() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(
                Arrays.asList("Mon"),
                Arrays.asList("P1", "P2", "P3"),
                new ArrayList<>()
        );

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 3 pairs: (3 + 2) / 3 = 1 row => 1 * 24 + 16 = 40
        assertEquals(40f, height, 0.001f);
    }

    @Test
    void testGetLegendHeightWithFourPairs() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(
                Arrays.asList("Mon"),
                Arrays.asList("P1", "P2", "P3", "P4"),
                new ArrayList<>()
        );

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 4 pairs: (4 + 2) / 3 = 2 rows => 2 * 24 + 16 = 64
        assertEquals(64f, height, 0.001f);
    }

    @Test
    void testSetDataStoresLabelsCorrectly() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<String> inputLabels = Arrays.asList("Mon", "Tue", "Wed");
        view.setData(inputLabels, new ArrayList<>(), new ArrayList<>());

        Field labelsField = EmotionLineChartView.class.getDeclaredField("labels");
        labelsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> storedLabels = (List<String>) labelsField.get(view);
        assertEquals(3, storedLabels.size());
        assertEquals("Mon", storedLabels.get(0));
    }

    @Test
    void testSetDataStoresPairNamesCorrectly() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<String> names = Arrays.asList("Happy-Sad", "Calm-Anxious");
        view.setData(new ArrayList<>(), names, new ArrayList<>());

        Field pairNamesField = EmotionLineChartView.class.getDeclaredField("pairNames");
        pairNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> stored = (List<String>) pairNamesField.get(view);
        assertEquals(2, stored.size());
        assertEquals("Happy-Sad", stored.get(0));
        assertEquals("Calm-Anxious", stored.get(1));
    }

    @Test
    void testSetDataStoresDailyValuesCorrectly() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<List<Float>> values = new ArrayList<>();
        values.add(Arrays.asList(5.0f, -3.0f, 7.0f));
        view.setData(Arrays.asList("A", "B", "C"), Arrays.asList("P1"), values);

        Field valuesField = EmotionLineChartView.class.getDeclaredField("pairDailyValues");
        valuesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<List<Float>> stored = (List<List<Float>>) valuesField.get(view);
        assertEquals(1, stored.size());
        assertEquals(3, stored.get(0).size());
        assertEquals(5.0f, stored.get(0).get(0), 0.001f);
        assertEquals(-3.0f, stored.get(0).get(1), 0.001f);
    }

    @Test
    void testPairColorsAreAllUnique() throws Exception {
        Field field = EmotionLineChartView.class.getDeclaredField("PAIR_COLORS");
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
    void testGetLegendHeightWithSixPairs() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<String> names = Arrays.asList("P1", "P2", "P3", "P4", "P5", "P6");
        view.setData(Arrays.asList("Mon"), names, new ArrayList<>());

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 6 pairs: (6 + 2) / 3 = 2 rows => 2 * 24 + 16 = 64
        assertEquals(64f, height, 0.001f);
    }

    @Test
    void testGetLegendHeightWithTenPairs() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            names.add("Pair " + i);
        }
        view.setData(Arrays.asList("Mon"), names, new ArrayList<>());

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 10 pairs: (10 + 2) / 3 = 4 rows => 4 * 24 + 16 = 112
        assertEquals(112f, height, 0.001f);
    }

    @Test
    void testSetDataWithNullLabelsDefaultsToEmptyList() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(null, Arrays.asList("P1"), new ArrayList<>());

        Field labelsField = EmotionLineChartView.class.getDeclaredField("labels");
        labelsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) labelsField.get(view);
        assertNotNull(labels);
        assertTrue(labels.isEmpty());
    }

    @Test
    void testSetDataWithNullPairNamesDefaultsToEmptyList() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(Arrays.asList("Mon"), null, new ArrayList<>());

        Field pairNamesField = EmotionLineChartView.class.getDeclaredField("pairNames");
        pairNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) pairNamesField.get(view);
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    @Test
    void testSetDataWithNullDailyValuesDefaultsToEmptyList() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(Arrays.asList("Mon"), Arrays.asList("P1"), null);

        Field valuesField = EmotionLineChartView.class.getDeclaredField("pairDailyValues");
        valuesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<List<Float>> values = (List<List<Float>>) valuesField.get(view);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    void testPairColorsMatchScatterChartColors() throws Exception {
        Field lineField = EmotionLineChartView.class.getDeclaredField("PAIR_COLORS");
        lineField.setAccessible(true);
        int[] lineColors = (int[]) lineField.get(null);

        Field scatterField = EmotionScatterChartView.class.getDeclaredField("PAIR_COLORS");
        scatterField.setAccessible(true);
        int[] scatterColors = (int[]) scatterField.get(null);

        assertEquals(lineColors.length, scatterColors.length,
                "Line chart and scatter chart should have same number of pair colors");
        for (int i = 0; i < lineColors.length; i++) {
            assertEquals(lineColors[i], scatterColors[i],
                    "Color at index " + i + " should match between charts");
        }
    }

    @Test
    void testGetLegendHeightWithTwoPairs() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        view.setData(Arrays.asList("Mon"), Arrays.asList("P1", "P2"), new ArrayList<>());

        Method method = EmotionLineChartView.class.getDeclaredMethod("getLegendHeight");
        method.setAccessible(true);
        float height = (float) method.invoke(view);
        // 2 pairs: (2 + 2) / 3 = 1 row => 1 * 24 + 16 = 40
        assertEquals(40f, height, 0.001f);
    }

    @Test
    void testSetDataWithMultiplePairValues() throws Exception {
        EmotionLineChartView view = new EmotionLineChartView(null);
        List<List<Float>> values = new ArrayList<>();
        values.add(Arrays.asList(1.0f, 2.0f, 3.0f));
        values.add(Arrays.asList(-1.0f, 0.0f, 1.0f));
        view.setData(Arrays.asList("A", "B", "C"), Arrays.asList("P1", "P2"), values);

        Field valuesField = EmotionLineChartView.class.getDeclaredField("pairDailyValues");
        valuesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<List<Float>> stored = (List<List<Float>>) valuesField.get(view);
        assertEquals(2, stored.size());
        assertEquals(3, stored.get(0).size());
        assertEquals(3, stored.get(1).size());
    }
}
