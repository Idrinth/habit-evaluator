package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SleepDistributionViewTest {

    @Test
    void testSetDataWithNullDoesNotThrow() {
        SleepDistributionView view = new SleepDistributionView(null);
        view.setData(null);
    }

    @Test
    void testSetDataWithNullDefaultsToEmptyArray() throws Exception {
        SleepDistributionView view = new SleepDistributionView(null);
        view.setData(null);

        Field field = SleepDistributionView.class.getDeclaredField("percentAsleep");
        field.setAccessible(true);
        double[] data = (double[]) field.get(view);
        assertNotNull(data);
        assertEquals(24, data.length);
        for (double v : data) {
            assertEquals(0.0, v, 0.001);
        }
    }

    @Test
    void testSetDataStoresValues() throws Exception {
        SleepDistributionView view = new SleepDistributionView(null);
        double[] input = new double[24];
        input[0] = 80.0;
        input[12] = 10.0;
        input[23] = 95.0;
        view.setData(input);

        Field field = SleepDistributionView.class.getDeclaredField("percentAsleep");
        field.setAccessible(true);
        double[] stored = (double[]) field.get(view);
        assertEquals(80.0, stored[0], 0.001);
        assertEquals(10.0, stored[12], 0.001);
        assertEquals(95.0, stored[23], 0.001);
    }

    @Test
    void testSetDataWithAllZeros() throws Exception {
        SleepDistributionView view = new SleepDistributionView(null);
        view.setData(new double[24]);

        Field field = SleepDistributionView.class.getDeclaredField("percentAsleep");
        field.setAccessible(true);
        double[] stored = (double[]) field.get(view);
        for (double v : stored) {
            assertEquals(0.0, v, 0.001);
        }
    }

    @Test
    void testSetDataWithFullValues() throws Exception {
        SleepDistributionView view = new SleepDistributionView(null);
        double[] input = new double[24];
        for (int i = 0; i < 24; i++) {
            input[i] = 100.0;
        }
        view.setData(input);

        Field field = SleepDistributionView.class.getDeclaredField("percentAsleep");
        field.setAccessible(true);
        double[] stored = (double[]) field.get(view);
        for (double v : stored) {
            assertEquals(100.0, v, 0.001);
        }
    }

    @Test
    void testSetDataReplacesExistingData() throws Exception {
        SleepDistributionView view = new SleepDistributionView(null);

        double[] first = new double[24];
        first[0] = 50.0;
        view.setData(first);

        double[] second = new double[24];
        second[0] = 75.0;
        view.setData(second);

        Field field = SleepDistributionView.class.getDeclaredField("percentAsleep");
        field.setAccessible(true);
        double[] stored = (double[]) field.get(view);
        assertEquals(75.0, stored[0], 0.001);
    }

    @Test
    void testPaddingConstantsArePositive() throws Exception {
        assertFieldPositive("PADDING_LEFT");
        assertFieldPositive("PADDING_RIGHT");
        assertFieldPositive("PADDING_TOP");
        assertFieldPositive("PADDING_BOTTOM");
    }

    @Test
    void testConstructorWithTwoArgs() {
        SleepDistributionView view = new SleepDistributionView(null, null);
        assertNotNull(view);
    }

    @Test
    void testConstructorWithThreeArgs() {
        SleepDistributionView view = new SleepDistributionView(null, null, 0);
        assertNotNull(view);
    }

    @Test
    void testDefaultDataIs24Hours() throws Exception {
        SleepDistributionView view = new SleepDistributionView(null);

        Field field = SleepDistributionView.class.getDeclaredField("percentAsleep");
        field.setAccessible(true);
        double[] data = (double[]) field.get(view);
        assertEquals(24, data.length);
    }

    private void assertFieldPositive(String fieldName) throws Exception {
        Field field = SleepDistributionView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        float value = field.getFloat(null);
        assertTrue(value > 0, fieldName + " should be positive");
    }
}
