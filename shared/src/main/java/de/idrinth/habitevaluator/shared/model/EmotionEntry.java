package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a recorded emotional state at a point in time.
 * Strength ranges from -10 (fully negative label) to +10 (fully positive label).
 */
@Entity
@Table(name = "emotion_entries")
public class EmotionEntry {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_pair_id", nullable = false)
    private EmotionPair emotionPair;

    @Column(nullable = false)
    private int strength;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public EmotionEntry() {
        this.id = UUID.randomUUID().toString();
        this.recordedAt = LocalDateTime.now();
    }

    public EmotionEntry(EmotionPair emotionPair, int strength, LocalDateTime recordedAt, String notes) {
        this();
        this.emotionPair = emotionPair;
        this.strength = Math.max(-10, Math.min(10, strength));
        this.recordedAt = recordedAt;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EmotionPair getEmotionPair() {
        return emotionPair;
    }

    public void setEmotionPair(EmotionPair emotionPair) {
        this.emotionPair = emotionPair;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = Math.max(-10, Math.min(10, strength));
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        EmotionEntry that = (EmotionEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
