package de.idrinth.habitevaluator.shared.model;

/**
 * Represents a weighted correlation between two events over a yearly timeframe.
 * The correlation strength is computed using a time-weighted Pearson coefficient
 * where newer entries carry more weight than older ones.
 */
public class EventCorrelation {

    private String eventA;
    private String eventB;
    private double correlation;
    private int sharedDays;

    public EventCorrelation() {
    }

    public EventCorrelation(String eventA, String eventB, double correlation, int sharedDays) {
        this.eventA = eventA;
        this.eventB = eventB;
        this.correlation = correlation;
        this.sharedDays = sharedDays;
    }

    public String getEventA() {
        return eventA;
    }

    public void setEventA(String eventA) {
        this.eventA = eventA;
    }

    public String getEventB() {
        return eventB;
    }

    public void setEventB(String eventB) {
        this.eventB = eventB;
    }

    public double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(double correlation) {
        this.correlation = correlation;
    }

    public int getSharedDays() {
        return sharedDays;
    }

    public void setSharedDays(int sharedDays) {
        this.sharedDays = sharedDays;
    }
}
