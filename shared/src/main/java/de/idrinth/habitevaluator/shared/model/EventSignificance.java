package de.idrinth.habitevaluator.shared.model;

/**
 * Represents the significance level of a positive diary event.
 * Each level maps to a fixed point value.
 */
public enum EventSignificance {
    MINOR(1),
    NORMAL(2),
    MAJOR(4);

    private final int points;

    EventSignificance(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
