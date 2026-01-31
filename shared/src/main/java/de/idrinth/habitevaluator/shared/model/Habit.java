package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a habit to be tracked and evaluated.
 */
@Entity
@Table(name = "habits", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "category_id", "user_id"})
})
public class Habit {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "category_id")
    private String categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private FrequencyType frequencyType;

    @Column(name = "target_frequency", nullable = false)
    private int targetFrequency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<HabitEntry> entries;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scoring_rule_id")
    private ScoringRule scoringRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Habit() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.entries = new ArrayList<>();
        this.frequencyType = FrequencyType.DAILY;
        this.targetFrequency = 1;
        this.scoringRule = new ScoringRule();
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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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
        entry.setHabit(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ScoringRule getScoringRule() {
        return scoringRule;
    }

    public void setScoringRule(ScoringRule scoringRule) {
        this.scoringRule = scoringRule;
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
