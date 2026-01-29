package de.idrinth.habitevaluator.shared.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Defines custom scoring rules that map weekly completion counts to points.
 * Supports the fixed point values: 0, 1, 2, 4, 8.
 * Each threshold defines the minimum completions per week required for that score.
 */
public class ScoringRule {

    private String id;
    private String name;
    private int thresholdFor1Point;
    private int thresholdFor2Points;
    private int thresholdFor4Points;
    private int thresholdFor8Points;

    public ScoringRule() {
        this.id = UUID.randomUUID().toString();
        // Default thresholds
        this.thresholdFor1Point = 1;
        this.thresholdFor2Points = 2;
        this.thresholdFor4Points = 4;
        this.thresholdFor8Points = 7;
    }

    public ScoringRule(String name) {
        this();
        this.name = name;
    }

    public ScoringRule(String name, int thresholdFor1Point, int thresholdFor2Points,
                       int thresholdFor4Points, int thresholdFor8Points) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        validateThresholds(thresholdFor1Point, thresholdFor2Points, thresholdFor4Points, thresholdFor8Points);
        this.thresholdFor1Point = thresholdFor1Point;
        this.thresholdFor2Points = thresholdFor2Points;
        this.thresholdFor4Points = thresholdFor4Points;
        this.thresholdFor8Points = thresholdFor8Points;
    }

    private void validateThresholds(int t1, int t2, int t4, int t8) {
        if (t1 < 0 || t2 < 0 || t4 < 0 || t8 < 0) {
            throw new IllegalArgumentException("Thresholds must be non-negative");
        }
        if (!(t1 <= t2 && t2 <= t4 && t4 <= t8)) {
            throw new IllegalArgumentException(
                "Thresholds must be in ascending order: thresholdFor1Point <= thresholdFor2Points <= thresholdFor4Points <= thresholdFor8Points");
        }
    }

    /**
     * Calculates the score based on weekly completion count.
     *
     * @param weeklyCompletions number of completions in the week
     * @return the score (0, 1, 2, 4, or 8)
     */
    public int calculateScore(int weeklyCompletions) {
        if (weeklyCompletions >= thresholdFor8Points) {
            return 8;
        } else if (weeklyCompletions >= thresholdFor4Points) {
            return 4;
        } else if (weeklyCompletions >= thresholdFor2Points) {
            return 2;
        } else if (weeklyCompletions >= thresholdFor1Point) {
            return 1;
        }
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThresholdFor1Point() {
        return thresholdFor1Point;
    }

    public void setThresholdFor1Point(int thresholdFor1Point) {
        validateThresholds(thresholdFor1Point, this.thresholdFor2Points, this.thresholdFor4Points, this.thresholdFor8Points);
        this.thresholdFor1Point = thresholdFor1Point;
    }

    public int getThresholdFor2Points() {
        return thresholdFor2Points;
    }

    public void setThresholdFor2Points(int thresholdFor2Points) {
        validateThresholds(this.thresholdFor1Point, thresholdFor2Points, this.thresholdFor4Points, this.thresholdFor8Points);
        this.thresholdFor2Points = thresholdFor2Points;
    }

    public int getThresholdFor4Points() {
        return thresholdFor4Points;
    }

    public void setThresholdFor4Points(int thresholdFor4Points) {
        validateThresholds(this.thresholdFor1Point, this.thresholdFor2Points, thresholdFor4Points, this.thresholdFor8Points);
        this.thresholdFor4Points = thresholdFor4Points;
    }

    public int getThresholdFor8Points() {
        return thresholdFor8Points;
    }

    public void setThresholdFor8Points(int thresholdFor8Points) {
        validateThresholds(this.thresholdFor1Point, this.thresholdFor2Points, this.thresholdFor4Points, thresholdFor8Points);
        this.thresholdFor8Points = thresholdFor8Points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoringRule that = (ScoringRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
