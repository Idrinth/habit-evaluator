package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DayPlannerServiceTest {

    private DayPlannerService service;

    @BeforeEach
    void setUp() {
        // Use a seeded random for deterministic tests
        service = new DayPlannerService(new Random(42));
    }

    private void addGroupToSlot(WeekPlannerSlot slot, PlannerGroup group) {
        Set<PlannerGroup> groups = new HashSet<>(slot.getGroups());
        groups.add(group);
        slot.setGroups(groups);
    }

    @Test
    void testSuggestActivityWithNullSlots() {
        List<PlannerActivity> activities = List.of(new PlannerActivity("Walk"));
        assertNull(service.suggestActivity(null, activities));
    }

    @Test
    void testSuggestActivityWithEmptySlots() {
        List<PlannerActivity> activities = List.of(new PlannerActivity("Walk"));
        assertNull(service.suggestActivity(Collections.emptyList(), activities));
    }

    @Test
    void testSuggestActivityWithNullActivities() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        addGroupToSlot(slot, new PlannerGroup("Fitness"));
        assertNull(service.suggestActivity(List.of(slot), null));
    }

    @Test
    void testSuggestActivityWithEmptyActivities() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        addGroupToSlot(slot, new PlannerGroup("Fitness"));
        assertNull(service.suggestActivity(List.of(slot), Collections.emptyList()));
    }

    @Test
    void testSuggestActivityWithNoGroupInSlots() {
        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        // No group set
        List<PlannerActivity> activities = List.of(new PlannerActivity("Walk"));
        assertNull(service.suggestActivity(List.of(slot), activities));
    }

    @Test
    void testSuggestActivityWithMatchingGroupAndActivity() {
        PlannerGroup group = new PlannerGroup("Fitness");

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        addGroupToSlot(slot, group);

        PlannerActivity activity = new PlannerActivity("Go for a walk");
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);
        activity.setGroups(groups);

        PlannerActivity result = service.suggestActivity(List.of(slot), List.of(activity));
        assertNotNull(result);
        assertEquals("Go for a walk", result.getName());
    }

    @Test
    void testSuggestActivityWithNoMatchingActivities() {
        PlannerGroup fitnessGroup = new PlannerGroup("Fitness");
        PlannerGroup creativeGroup = new PlannerGroup("Creative");

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        addGroupToSlot(slot, fitnessGroup);

        // Activity belongs to creative group, not fitness
        PlannerActivity activity = new PlannerActivity("Paint");
        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(creativeGroup);
        activity.setGroups(groups);

        PlannerActivity result = service.suggestActivity(List.of(slot), List.of(activity));
        assertNull(result);
    }

    @Test
    void testSuggestActivityReturnsSomethingFromMultiple() {
        PlannerGroup group = new PlannerGroup("Fitness");

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        addGroupToSlot(slot, group);

        Set<PlannerGroup> groups = new HashSet<>();
        groups.add(group);

        PlannerActivity a1 = new PlannerActivity("Walk");
        a1.setGroups(groups);
        PlannerActivity a2 = new PlannerActivity("Run");
        a2.setGroups(groups);
        PlannerActivity a3 = new PlannerActivity("Swim");
        a3.setGroups(groups);

        PlannerActivity result = service.suggestActivity(List.of(slot), List.of(a1, a2, a3));
        assertNotNull(result);
        assertTrue(List.of("Walk", "Run", "Swim").contains(result.getName()));
    }

    @Test
    void testSuggestActivityWithMultipleGroupsPerSlot() {
        PlannerGroup fitnessGroup = new PlannerGroup("Fitness");
        PlannerGroup creativeGroup = new PlannerGroup("Creative");

        WeekPlannerSlot slot = new WeekPlannerSlot(1, 9);
        Set<PlannerGroup> slotGroups = new HashSet<>();
        slotGroups.add(fitnessGroup);
        slotGroups.add(creativeGroup);
        slot.setGroups(slotGroups);

        PlannerActivity walkActivity = new PlannerActivity("Walk");
        walkActivity.setGroups(Set.of(fitnessGroup));
        PlannerActivity paintActivity = new PlannerActivity("Paint");
        paintActivity.setGroups(Set.of(creativeGroup));

        PlannerActivity result = service.suggestActivity(List.of(slot), List.of(walkActivity, paintActivity));
        assertNotNull(result);
        assertTrue(List.of("Walk", "Paint").contains(result.getName()));
    }

    @Test
    void testCalculateConfirmationRateZeroTotal() {
        assertEquals(0.0, service.calculateConfirmationRate(0, 0));
    }

    @Test
    void testCalculateConfirmationRateNegativeTotal() {
        assertEquals(0.0, service.calculateConfirmationRate(-1, 0));
    }

    @Test
    void testCalculateConfirmationRateAllConfirmed() {
        assertEquals(1.0, service.calculateConfirmationRate(10, 10));
    }

    @Test
    void testCalculateConfirmationRateHalfConfirmed() {
        assertEquals(0.5, service.calculateConfirmationRate(10, 5), 0.001);
    }

    @Test
    void testCalculateConfirmationRateCappedAt1() {
        assertEquals(1.0, service.calculateConfirmationRate(5, 10));
    }

    @Test
    void testGetWeekSlotSummaryNull() {
        int[] summary = service.getWeekSlotSummary(null);
        assertEquals(0, summary[0]);
        assertEquals(168, summary[1]);
    }

    @Test
    void testGetWeekSlotSummaryEmpty() {
        int[] summary = service.getWeekSlotSummary(Collections.emptyList());
        assertEquals(0, summary[0]);
        assertEquals(168, summary[1]);
    }

    @Test
    void testGetWeekSlotSummaryWithSlots() {
        PlannerGroup group = new PlannerGroup("Fitness");
        WeekPlannerSlot s1 = new WeekPlannerSlot(1, 9);
        addGroupToSlot(s1, group);
        WeekPlannerSlot s2 = new WeekPlannerSlot(1, 10);
        addGroupToSlot(s2, group);
        WeekPlannerSlot s3 = new WeekPlannerSlot(2, 14);
        // s3 has no group

        int[] summary = service.getWeekSlotSummary(List.of(s1, s2, s3));
        assertEquals(2, summary[0]);
        assertEquals(168, summary[1]);
    }

    @Test
    void testDefaultConstructorUsesRandomWithoutSeed() {
        DayPlannerService defaultService = new DayPlannerService();
        assertNotNull(defaultService);
    }
}
