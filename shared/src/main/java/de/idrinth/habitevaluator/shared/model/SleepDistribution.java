package de.idrinth.habitevaluator.shared.model;

/**
 * Holds sleep distribution data across a 24-hour period.
 * Each hour bucket contains the percentage of days the user was asleep
 * during that hour.
 */
public class SleepDistribution {

    private double[] percentAsleep;

    public SleepDistribution() {
        this.percentAsleep = new double[24];
    }

    public SleepDistribution(double[] percentAsleep) {
        this.percentAsleep = percentAsleep;
    }

    public double[] getPercentAsleep() {
        return percentAsleep;
    }

    public void setPercentAsleep(double[] percentAsleep) {
        this.percentAsleep = percentAsleep;
    }
}
