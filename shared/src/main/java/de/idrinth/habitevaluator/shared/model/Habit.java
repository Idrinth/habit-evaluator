package de.idrinth.habitevaluator.shared.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a habit to be tracked and evaluated.
 */
public class Habit {

    private String id;
    private String name;
    private String description;
    private FrequencyType frequencyType;
    private int targetFrequency;
    private LocalDateTime createdAt;
    private List<HabitEntry> entries;

    public Habit() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.entries = new ArrayList<>();
        this.frequencyType = FrequencyType.DAILY;
        this.targetFrequency = 1;
    }

    public Habit(String name, String description) {
        this();
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

    public int getTargetFrequency() {
        return targetFrequency;
    }

    public void setTargetFrequency(int targetFrequency) {
        this.targetFrequency = targetFrequency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<HabitEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<HabitEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(HabitEntry entry) {
        this.entries.add(entry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Habit habit = (Habit) o;
        return Objects.equals(id, habit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
