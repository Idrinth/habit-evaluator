package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unique diary description text for a user.
 * Multiple diary entries can reference the same description for easier analysis.
 * Matching is case-insensitive via the descriptionLower field.
 */
@Entity
@Table(name = "diary_references")
public class DiaryReference {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String description;

    @Column(name = "description_lower", nullable = false)
    private String descriptionLower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public DiaryReference() {
        this.id = UUID.randomUUID().toString();
    }

    public DiaryReference(String description) {
        this();
        setDescription(description);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionLower = description != null ? description.toLowerCase() : null;
    }

    public String getDescriptionLower() {
        return descriptionLower;
    }

    public void setDescriptionLower(String descriptionLower) {
        this.descriptionLower = descriptionLower;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiaryReference that = (DiaryReference) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return description;
    }
}
