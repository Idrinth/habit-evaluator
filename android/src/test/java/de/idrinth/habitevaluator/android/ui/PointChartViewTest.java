package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointChartViewTest {

    /**
     * Creates a PointChartView instance using Mockito CALLS_REAL_METHODS to bypass
     * the constructor which calls MaterialColors.getColor() and requires
     * a real Android Context.
     */
    private PointChartView createInstanceWithoutInit() throws Exception {
        PointChartView view = Mockito.mock(PointChartView.class, Mockito.CALLS_REAL_METHODS);
        // Initialize the dailyPoints list since constructor was bypassed
        Field field = PointChartView.class.getDeclaredField("dailyPoints");
        field.setAccessible(true);
        field.set(view, new ArrayList<Integer>());
        return view;
    }

    @Test
    void testCalculateTrendLineWithEmptyData() throws Exception {
        PointChartView view = createInstanceWithoutInit();

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(0, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineWithSinglePoint() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(5));

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(5, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineWithTwoPoints() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(2, 4));

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(2.0, trend[0], 0.001);
        assertEquals(2.0, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineWithFlatData() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(3, 3, 3, 3));

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(3, trend[1], 0.001);
    }

    @Test
    void testCalculateTrendLineSkipsZeroValues() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(0, 4, 0, 8));

        double[] trend = invokeCalculateTrendLine(view);
        // Only indices 1 (val 4) and 3 (val 8) contribute
        assertTrue(trend[0] > 0, "Slope should be positive for increasing data");
    }

    @Test
    void testCalculateTrendLineWithAllZeros() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(0, 0, 0));

        double[] trend = invokeCalculateTrendLine(view);
        assertEquals(0, trend[0], 0.001);
        assertEquals(0, trend[1], 0.001);
    }

    @Test
    void testGetTrendY() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        double[] trend = {2.0, 3.0}; // slope=2, intercept=3

        Method method = PointChartView.class.getDeclaredMethod("getTrendY", double[].class, int.class);
        method.setAccessible(true);
        double result = (double) method.invoke(view, trend, 5);

        assertEquals(13.0, result, 0.001); // 2*5 + 3 = 13
    }

    @Test
    void testGetTrendYAtZero() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        double[] trend = {1.5, 10.0};

        Method method = PointChartView.class.getDeclaredMethod("getTrendY", double[].class, int.class);
        method.setAccessible(true);
        double result = (double) method.invoke(view, trend, 0);

        assertEquals(10.0, result, 0.001); // 1.5*0 + 10 = 10
    }

    @Test
    void testGetTrendYWithNegativeSlope() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        double[] trend = {-1.0, 5.0};

        Method method = PointChartView.class.getDeclaredMethod("getTrendY", double[].class, int.class);
        method.setAccessible(true);
        double result = (double) method.invoke(view, trend, 3);

        assertEquals(2.0, result, 0.001); // -1*3 + 5 = 2
    }

    @Test
    void testCalculateTrendLineWithNegativeValues() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(-2, -4, -6));

        double[] trend = invokeCalculateTrendLine(view);
        assertTrue(trend[0] < 0, "Slope should be negative for decreasing data");
    }

    @Test
    void testCalculateTrendLineWithMixedValues() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(1, 5, 3, 7, 2));

        double[] trend = invokeCalculateTrendLine(view);
        assertNotNull(trend);
        assertEquals(2, trend.length);
    }

    @Test
    void testCalculateTrendLineWithSingleNonZeroValue() throws Exception {
        PointChartView view = createInstanceWithoutInit();
        setDailyPoints(view, Arrays.asList(0, 0, 5, 0, 0));

        double[] trend = invokeCalculateTrendLine(view);
        // Only one non-zero point, n < 2, so flat line at avg
        assertEquals(0, trend[0], 0.001);
        assertEquals(5, trend[1], 0.001);
    }

    @Test
    void testBarGapFractionConstant() throws Exception {
        Field field = PointChartView.class.getDeclaredField("BAR_GAP_FRACTION");
        field.setAccessible(true);
        float gap = field.getFloat(null);
        assertTrue(gap > 0 && gap < 1, "BAR_GAP_FRACTION should be between 0 and 1");
    }

    @Test
    void testBarCornerRadiusConstant() throws Exception {
        Field field = PointChartView.class.getDeclaredField("BAR_CORNER_RADIUS_DP");
        field.setAccessible(true);
        float radius = field.getFloat(null);
        assertTrue(radius >= 0, "BAR_CORNER_RADIUS_DP should be non-negative");
    }

    @SuppressWarnings("unchecked")
    private void setDailyPoints(PointChartView view, List<Integer> points) throws Exception {
        Field field = PointChartView.class.getDeclaredField("dailyPoints");
        field.setAccessible(true);
        List<Integer> list = (List<Integer>) field.get(view);
        list.clear();
        list.addAll(points);
    }

    private double[] invokeCalculateTrendLine(PointChartView view) throws Exception {
        Method method = PointChartView.class.getDeclaredMethod("calculateTrendLine");
        method.setAccessible(true);
        return (double[]) method.invoke(view);
    }
}
