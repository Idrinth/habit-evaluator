package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single entry/completion of a habit.
 */
@Entity
@Table(name = "habit_entries")
public class HabitEntry {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private int value;

    public HabitEntry() {
        this.id = UUID.randomUUID().toString();
        this.completedAt = LocalDateTime.now();
        this.value = 1;
    }

    public HabitEntry(String habitId) {
        this();
        this.habit = new Habit();
        this.habit.setId(habitId);
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

    @Transient
    public String getHabitId() {
        return habit != null ? habit.getId() : null;
    }

    public void setHabitId(String habitId) {
        if (this.habit == null) {
            this.habit = new Habit();
        }
        this.habit.setId(habitId);
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
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
