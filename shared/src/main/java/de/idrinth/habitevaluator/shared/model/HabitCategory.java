package de.idrinth.habitevaluator.shared.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a user-defined category for organizing habits.
 */
public class HabitCategory {

    private String id;
    private String name;
    private String description;
    private String color;

    public HabitCategory() {
        this.id = UUID.randomUUID().toString();
    }

    public HabitCategory(String name) {
        this();
        this.name = name;
    }

    public HabitCategory(String name, String description, String color) {
        this(name);
        this.description = description;
        this.color = color;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitCategory that = (HabitCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
