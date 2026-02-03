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
 * Represents a pair of opposing emotions (e.g. "listless" - "active").
 * The negative label sits at -10, the positive label at +10.
 */
@Entity
@Table(name = "emotion_pairs")
public class EmotionPair {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "negative_label", nullable = false)
    private String negativeLabel;

    @Column(name = "positive_label", nullable = false)
    private String positiveLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public EmotionPair() {
        this.id = UUID.randomUUID().toString();
    }

    public EmotionPair(String negativeLabel, String positiveLabel) {
        this();
        this.negativeLabel = negativeLabel;
        this.positiveLabel = positiveLabel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNegativeLabel() {
        return negativeLabel;
    }

    public void setNegativeLabel(String negativeLabel) {
        this.negativeLabel = negativeLabel;
    }

    public String getPositiveLabel() {
        return positiveLabel;
    }

    public void setPositiveLabel(String positiveLabel) {
        this.positiveLabel = positiveLabel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return negativeLabel + " \u2014 " + positiveLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionPair that = (EmotionPair) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
