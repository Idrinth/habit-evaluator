package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for day planner random activity suggestion logic.
 * When a time slot starts, first picks a random group from those assigned to the slot,
 * then picks a random activity within that group. This two-step selection ensures
 * no single large group dominates suggestions.
 */
public class DayPlannerService {

    private final Random random;

    public DayPlannerService() {
        this.random = new Random();
    }

    public DayPlannerService(Random random) {
        this.random = random;
    }

    /**
     * Suggests a random activity for a given time slot.
     * First picks a random group from those assigned to the slot's time,
     * then picks a random activity within that group.
     *
     * @param slotsForHour all slots assigned to this day-of-week and hour
     * @param activitiesByGroup a function-like list of activities per group
     * @return suggested activity, or null if no activities are available
     */
    public PlannerActivity suggestActivity(
            List<WeekPlannerSlot> slotsForHour,
            List<PlannerActivity> allActivities
    ) {
        if (slotsForHour == null || slotsForHour.isEmpty()) {
            return null;
        }
        if (allActivities == null || allActivities.isEmpty()) {
            return null;
        }

        // Collect unique groups from slots
        List<PlannerGroup> groups = new ArrayList<>();
        for (WeekPlannerSlot slot : slotsForHour) {
            if (slot.getGroups() != null) {
                for (PlannerGroup group : slot.getGroups()) {
                    if (!groups.contains(group)) {
                        groups.add(group);
                    }
                }
            }
        }
        if (groups.isEmpty()) {
            return null;
        }

        // Pick a random group
        PlannerGroup selectedGroup = groups.get(random.nextInt(groups.size()));

        // Find activities in the selected group
        List<PlannerActivity> groupActivities = new ArrayList<>();
        for (PlannerActivity activity : allActivities) {
            if (activity.getGroups() != null && activity.getGroups().contains(selectedGroup)) {
                groupActivities.add(activity);
            }
        }
        if (groupActivities.isEmpty()) {
            return null;
        }

        // Pick a random activity
        return groupActivities.get(random.nextInt(groupActivities.size()));
    }

    /**
     * Calculates the confirmation rate for a given user's slot confirmations.
     *
     * @param totalConfirmations total number of confirmations
     * @param confirmedCount number of confirmed (not denied) entries
     * @return confirmation rate between 0.0 and 1.0, or 0.0 if no confirmations
     */
    public double calculateConfirmationRate(int totalConfirmations, int confirmedCount) {
        if (totalConfirmations <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) confirmedCount / totalConfirmations);
    }

    /**
     * Counts filled slots (slots with a group assigned) vs total possible slots in a week.
     *
     * @param slots all slots for the user
     * @return array of [filledCount, totalPossible] where totalPossible is 7 * 24 = 168
     */
    public int[] getWeekSlotSummary(List<WeekPlannerSlot> slots) {
        int filled = 0;
        if (slots != null) {
            for (WeekPlannerSlot slot : slots) {
                if (slot.getGroups() != null && !slot.getGroups().isEmpty()) {
                    filled++;
                }
            }
        }
        return new int[]{filled, 7 * 24};
    }
}
