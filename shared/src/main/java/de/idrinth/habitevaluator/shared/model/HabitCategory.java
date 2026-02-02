package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a user-defined category for organizing habits.
 */
@Entity
@Table(name = "habit_categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class HabitCategory {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @jakarta.persistence.Transient
    private Map<String, String> nameTranslations = new HashMap<>();

    @jakarta.persistence.Transient
    private Map<String, String> descriptionTranslations = new HashMap<>();

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, String> getNameTranslations() {
        return nameTranslations;
    }

    public void setNameTranslations(Map<String, String> nameTranslations) {
        this.nameTranslations = nameTranslations != null ? nameTranslations : new HashMap<>();
    }

    public Map<String, String> getDescriptionTranslations() {
        return descriptionTranslations;
    }

    public void setDescriptionTranslations(Map<String, String> descriptionTranslations) {
        this.descriptionTranslations = descriptionTranslations != null ? descriptionTranslations : new HashMap<>();
    }

    public String getDisplayName(String language) {
        if (language != null && nameTranslations.containsKey(language)) {
            String translated = nameTranslations.get(language);
            if (translated != null && !translated.isEmpty()) {
                return translated;
            }
        }
        return name;
    }

    public String getDisplayDescription(String language) {
        if (language != null && descriptionTranslations.containsKey(language)) {
            String translated = descriptionTranslations.get(language);
            if (translated != null && !translated.isEmpty()) {
                return translated;
            }
        }
        return description;
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
