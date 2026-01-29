package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single entry/completion of a habit.
 */
public class HabitEntry {

    private String id;
    private String habitId;
    private LocalDateTime completedAt;
    private String notes;
    private int value;

    public HabitEntry() {
        this.id = UUID.randomUUID().toString();
        this.completedAt = LocalDateTime.now();
        this.value = 1;
    }

    public HabitEntry(String habitId) {
        this();
        this.habitId = habitId;
    }

    public HabitEntry(String habitId, String notes) {
        this(habitId);
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitEntry that = (HabitEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
