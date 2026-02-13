package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SleepGraphViewTest {

    @Test
    void testSetDataWithNullListsDoesNotThrow() {
        SleepGraphView view = new SleepGraphView(null);
        view.setData(null, null, 0f);
    }

    @Test
    void testSetDataWithEmptyListsDoesNotThrow() {
        SleepGraphView view = new SleepGraphView(null);
        view.setData(new ArrayList<>(), new ArrayList<>(), 0f);
    }

    @Test
    void testSetDataWithValidData() {
        SleepGraphView view = new SleepGraphView(null);
        view.setData(
                Arrays.asList("Mon", "Tue", "Wed"),
                Arrays.asList(7.5f, 8.0f, 6.5f),
                7.3f
        );
    }

    @Test
    void testSetBarColorDoesNotThrow() {
        SleepGraphView view = new SleepGraphView(null);
        view.setBarColor(0xFF00FF00);
    }

    @Test
    void testSetAverageColorDoesNotThrow() {
        SleepGraphView view = new SleepGraphView(null);
        view.setAverageColor(0xFFFF0000);
    }

    @Test
    void testSetValueFormatDoesNotThrow() {
        SleepGraphView view = new SleepGraphView(null);
        view.setValueFormat("%.2f");
    }

    @Test
    void testLightenColorWhite() throws Exception {
        Method method = SleepGraphView.class.getDeclaredMethod("lightenColor", int.class, float.class);
        method.setAccessible(true);

        // White stays white
        int white = 0xFFFFFFFF;
        int result = (int) method.invoke(null, white, 0.5f);
        assertEquals(0xFF, (result >> 24) & 0xFF, "Alpha should be preserved");
        assertEquals(0xFF, (result >> 16) & 0xFF, "Red should stay 255");
        assertEquals(0xFF, (result >> 8) & 0xFF, "Green should stay 255");
        assertEquals(0xFF, result & 0xFF, "Blue should stay 255");
    }

    @Test
    void testLightenColorBlack() throws Exception {
        Method method = SleepGraphView.class.getDeclaredMethod("lightenColor", int.class, float.class);
        method.setAccessible(true);

        // Black with 50% lightening -> mid-gray
        int black = 0xFF000000;
        int result = (int) method.invoke(null, black, 0.5f);
        assertEquals(0xFF, (result >> 24) & 0xFF, "Alpha should be preserved");
        assertEquals(127, (result >> 16) & 0xFF, "Red channel");
        assertEquals(127, (result >> 8) & 0xFF, "Green channel");
        assertEquals(127, result & 0xFF, "Blue channel");
    }

    @Test
    void testLightenColorPreservesAlpha() throws Exception {
        Method method = SleepGraphView.class.getDeclaredMethod("lightenColor", int.class, float.class);
        method.setAccessible(true);

        int semiTransparent = 0x80FF0000; // 50% transparent red
        int result = (int) method.invoke(null, semiTransparent, 0.5f);
        assertEquals(0x80, (result >> 24) & 0xFF, "Alpha should be preserved");
    }

    @Test
    void testLightenColorWithZeroFactor() throws Exception {
        Method method = SleepGraphView.class.getDeclaredMethod("lightenColor", int.class, float.class);
        method.setAccessible(true);

        int color = 0xFF804020;
        int result = (int) method.invoke(null, color, 0f);
        assertEquals(color, result, "Zero factor should not change the color");
    }

    @Test
    void testCalculateTrendLineWithEmptyData() throws Exception {
        SleepGraphView view = new SleepGraphView(null);
        setValues(view, new ArrayList<>());

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(0, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineWithSingleValue() throws Exception {
        SleepGraphView view = new SleepGraphView(null);
        setValues(view, Arrays.asList(7.5f));

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(7.5, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineWithIncreasingValues() throws Exception {
        SleepGraphView view = new SleepGraphView(null);
        setValues(view, Arrays.asList(6.0f, 7.0f, 8.0f));

        double[] trend = invokeCalculateTrendLine(view);
        assertTrue(trend[0] > 0, "Slope should be positive for increasing data");
    }

    @Test
    void testCalculateTrendLineSkipsZeroValues() throws Exception {
        SleepGraphView view = new SleepGraphView(null);
        setValues(view, Arrays.asList(0f, 8.0f, 0f, 6.0f));

        double[] trend = invokeCalculateTrendLine(view);
        // Only indices 1 (val 8) and 3 (val 6) contribute
        assertTrue(trend[0] < 0, "Slope should be negative for decreasing data points");
    }

    @Test
    void testCalculateTrendLineWithAllZeros() throws Exception {
        SleepGraphView view = new SleepGraphView(null);
        setValues(view, Arrays.asList(0f, 0f, 0f));

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(0, trend[1], 0.001);
    }

    @Test
    void testGetTrendY() throws Exception {
        SleepGraphView view = new SleepGraphView(null);
        double[] trend = {0.5, 6.0};

        Method method = SleepGraphView.class.getDeclaredMethod("getTrendY", double[].class, int.class);
        method.setAccessible(true);
        double result = (double) method.invoke(view, trend, 4);

        assertEquals(8.0, result, 0.001); // 0.5*4 + 6 = 8
    }

    @Test
    void testConstructorWithTwoArgs() {
        SleepGraphView view = new SleepGraphView(null, null);
        assertNotNull(view);
    }

    @Test
    void testConstructorWithThreeArgs() {
        SleepGraphView view = new SleepGraphView(null, null, 0);
        assertNotNull(view);
    }

    @SuppressWarnings("unchecked")
    private void setValues(SleepGraphView view, List<Float> newValues) throws Exception {
        Field field = SleepGraphView.class.getDeclaredField("values");
        field.setAccessible(true);
        List<Float> list = (List<Float>) field.get(view);
        if (list == null) {
            field.set(view, new ArrayList<>(newValues));
        } else {
            list.clear();
            list.addAll(newValues);
        }
    }

    private double[] invokeCalculateTrendLine(SleepGraphView view) throws Exception {
        Method method = SleepGraphView.class.getDeclaredMethod("calculateTrendLine");
        method.setAccessible(true);
        return (double[]) method.invoke(view);
    }
}
