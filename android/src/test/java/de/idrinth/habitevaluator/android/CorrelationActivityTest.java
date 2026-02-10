package de.idrinth.habitevaluator.android;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.EventCorrelation;

import static org.junit.Assert.*;

public class CorrelationActivityTest {

    private static final String ALL = "All";

    private List<EventCorrelation> correlations;

    @Before
    public void setUp() {
        correlations = new ArrayList<>();
        correlations.add(new EventCorrelation("Habit: Eat Breakfast", "Sleep Hours", 0.471, 30));
        correlations.add(new EventCorrelation("Sleep Hours", "Emotion: fatigued — energetic", -0.226, 20));
        correlations.add(new EventCorrelation("Diary Points", "Sleep Hours", 0.387, 25));
        correlations.add(new EventCorrelation("Habit: Walking", "Diary Points", 0.35, 15));
    }

    @Test
    public void testAllFiltersReturnEverything() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(correlations, ALL, ALL, ALL);
        assertEquals(4, result.size());
    }

    @Test
    public void testSourceFilterMatchesOnlyEventA() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                correlations, ALL, "Sleep Hours", ALL);
        assertEquals(1, result.size());
        assertEquals("Sleep Hours", result.get(0).getEventA());
        assertEquals("Emotion: fatigued — energetic", result.get(0).getEventB());
    }

    @Test
    public void testTargetFilterMatchesOnlyEventB() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                correlations, ALL, ALL, "Sleep Hours");
        assertEquals(2, result.size());
        for (EventCorrelation corr : result) {
            assertEquals("Sleep Hours", corr.getEventB());
        }
    }

    @Test
    public void testTargetFilterDoesNotMatchEventA() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                correlations, ALL, ALL, "Habit: Eat Breakfast");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSourceFilterDoesNotMatchEventB() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                correlations, ALL, "Emotion: fatigued — energetic", ALL);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testBothFiltersApplied() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                correlations, ALL, "Diary Points", "Sleep Hours");
        assertEquals(1, result.size());
        assertEquals("Diary Points", result.get(0).getEventA());
        assertEquals("Sleep Hours", result.get(0).getEventB());
    }

    @Test
    public void testNoMatchesReturnsEmpty() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                correlations, ALL, "Nonexistent", "Also Missing");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyCorrelationsReturnsEmpty() {
        List<EventCorrelation> result = CorrelationActivity.filterCorrelations(
                new ArrayList<>(), ALL, ALL, "Sleep Hours");
        assertTrue(result.isEmpty());
    }
}
