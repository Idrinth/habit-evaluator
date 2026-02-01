package de.idrinth.habitevaluator.shared.api;

import de.idrinth.habitevaluator.shared.model.Habit;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for bidirectional sync between local and remote storage.
 * Contains habits with their entries to be merged during sync.
 */
public class SyncData {

    private List<Habit> habits;

    public SyncData() {
        this.habits = new ArrayList<>();
    }

    public SyncData(List<Habit> habits) {
        this.habits = habits != null ? habits : new ArrayList<>();
    }

    public List<Habit> getHabits() {
        return habits;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
    }
}
