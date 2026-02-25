package de.idrinth.habitevaluator.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a normalized, case-insensitive person tag for a user.
 * Multiple ActivityLog entries can reference the same PersonTag via an n:m join table,
 * enabling case-insensitive autocomplete and deduplication of person names.
 */
@Entity
@Table(name = "person_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name_lower", "user_id"})
})
public class PersonTag {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_lower", nullable = false)
    private String nameLower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public PersonTag() {
        this.id = UUID.randomUUID().toString();
    }

    public PersonTag(String name) {
        this();
        setName(name);
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
        this.nameLower = name != null ? name.toLowerCase() : null;
    }

    public String getNameLower() {
        return nameLower;
    }

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower;
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
        PersonTag that = (PersonTag) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
