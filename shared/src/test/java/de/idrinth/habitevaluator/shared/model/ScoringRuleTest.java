package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoringRuleTest {

    @Test
    void testDefaultConstructorSetsDefaultThresholds() {
        ScoringRule rule = new ScoringRule();
        assertEquals(1, rule.getThresholdFor1Point());
        assertEquals(2, rule.getThresholdFor2Points());
        assertEquals(4, rule.getThresholdFor4Points());
        assertEquals(7, rule.getThresholdFor8Points());
        assertNotNull(rule.getId());
    }

    @Test
    void testNameConstructor() {
        ScoringRule rule = new ScoringRule("Custom Rule");
        assertEquals("Custom Rule", rule.getName());
        assertNotNull(rule.getId());
    }

    @Test
    void testFullConstructor() {
        ScoringRule rule = new ScoringRule("Full", 1, 3, 5, 7);
        assertEquals("Full", rule.getName());
        assertEquals(1, rule.getThresholdFor1Point());
        assertEquals(3, rule.getThresholdFor2Points());
        assertEquals(5, rule.getThresholdFor4Points());
        assertEquals(7, rule.getThresholdFor8Points());
    }

    @Test
    void testCalculateScoreReturns0ForNoCompletions() {
        ScoringRule rule = new ScoringRule();
        assertEquals(0, rule.calculateScore(0));
    }

    @Test
    void testCalculateScoreReturns1ForMinimumCompletion() {
        ScoringRule rule = new ScoringRule();
        assertEquals(1, rule.calculateScore(1));
    }

    @Test
    void testCalculateScoreReturns2() {
        ScoringRule rule = new ScoringRule();
        assertEquals(2, rule.calculateScore(2));
        assertEquals(2, rule.calculateScore(3));
    }

    @Test
    void testCalculateScoreReturns4() {
        ScoringRule rule = new ScoringRule();
        assertEquals(4, rule.calculateScore(4));
        assertEquals(4, rule.calculateScore(5));
        assertEquals(4, rule.calculateScore(6));
    }

    @Test
    void testCalculateScoreReturns8() {
        ScoringRule rule = new ScoringRule();
        assertEquals(8, rule.calculateScore(7));
        assertEquals(8, rule.calculateScore(10));
    }

    @Test
    void testCalculateScoreWithCustomThresholds() {
        ScoringRule rule = new ScoringRule("Custom", 2, 4, 6, 10);
        assertEquals(0, rule.calculateScore(1));
        assertEquals(1, rule.calculateScore(2));
        assertEquals(2, rule.calculateScore(4));
        assertEquals(4, rule.calculateScore(6));
        assertEquals(8, rule.calculateScore(10));
    }

    @Test
    void testValidateThresholdsRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringRule("Bad", -1, 2, 4, 7));
    }

    @Test
    void testValidateThresholdsRejectsNonAscending() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoringRule("Bad", 5, 3, 4, 7));
    }

    @Test
    void testSetThresholdFor1PointValidates() {
        ScoringRule rule = new ScoringRule();
        assertThrows(IllegalArgumentException.class,
                () -> rule.setThresholdFor1Point(10));
    }

    @Test
    void testSetThresholdFor2PointsValidates() {
        ScoringRule rule = new ScoringRule();
        assertThrows(IllegalArgumentException.class,
                () -> rule.setThresholdFor2Points(10));
    }

    @Test
    void testSetThresholdFor4PointsValidates() {
        ScoringRule rule = new ScoringRule();
        assertThrows(IllegalArgumentException.class,
                () -> rule.setThresholdFor4Points(10));
    }

    @Test
    void testSetThresholdFor8PointsValidates() {
        ScoringRule rule = new ScoringRule();
        assertThrows(IllegalArgumentException.class,
                () -> rule.setThresholdFor8Points(1));
    }

    @Test
    void testEqualsSameId() {
        ScoringRule rule1 = new ScoringRule();
        ScoringRule rule2 = new ScoringRule();
        rule2.setId(rule1.getId());
        assertEquals(rule1, rule2);
    }

    @Test
    void testEqualsDifferentId() {
        ScoringRule rule1 = new ScoringRule();
        ScoringRule rule2 = new ScoringRule();
        assertNotEquals(rule1, rule2);
    }

    @Test
    void testEqualsNull() {
        ScoringRule rule = new ScoringRule();
        assertNotEquals(null, rule);
    }

    @Test
    void testEqualsSameObject() {
        ScoringRule rule = new ScoringRule();
        assertEquals(rule, rule);
    }

    @Test
    void testHashCodeConsistentWithEquals() {
        ScoringRule rule1 = new ScoringRule();
        ScoringRule rule2 = new ScoringRule();
        rule2.setId(rule1.getId());
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    void testAllEqualThresholdsAreValid() {
        ScoringRule rule = new ScoringRule("Equal", 3, 3, 3, 3);
        assertEquals(8, rule.calculateScore(3));
        assertEquals(0, rule.calculateScore(2));
    }
}
