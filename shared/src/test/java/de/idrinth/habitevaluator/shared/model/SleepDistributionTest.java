package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SleepDistributionTest {

    @Test
    void testDefaultConstructor() {
        SleepDistribution dist = new SleepDistribution();
        assertNotNull(dist.getPercentAsleep());
        assertEquals(24, dist.getPercentAsleep().length);
        for (double v : dist.getPercentAsleep()) {
            assertEquals(0.0, v, 0.01);
        }
    }

    @Test
    void testParameterizedConstructor() {
        double[] data = new double[24];
        data[0] = 85.5;
        data[23] = 92.0;
        SleepDistribution dist = new SleepDistribution(data);
        assertEquals(85.5, dist.getPercentAsleep()[0], 0.01);
        assertEquals(92.0, dist.getPercentAsleep()[23], 0.01);
    }

    @Test
    void testSetPercentAsleep() {
        SleepDistribution dist = new SleepDistribution();
        double[] data = new double[24];
        data[12] = 50.0;
        dist.setPercentAsleep(data);
        assertEquals(50.0, dist.getPercentAsleep()[12], 0.01);
    }
}
