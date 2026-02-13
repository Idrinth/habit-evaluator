package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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
}
