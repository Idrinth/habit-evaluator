package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a positive event recorded in the diary.
 */
@Entity
@Table(name = "diary_entries")
public class DiaryEntry {

    @Id
    @Column(length = 36)
    private String id;

    /**
     * Legacy field for backward compatibility. New entries should use diaryReference.
     * This field is kept nullable to support migration from older data.
     * Column name kept as 'description' for backward compatibility with existing databases.
     */
    @Column(name = "description", nullable = true)
    private String legacyDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_reference_id")
    private DiaryReference diaryReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSignificance significance;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public DiaryEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.eventDate = LocalDate.now();
        this.significance = EventSignificance.NORMAL;
    }

    public DiaryEntry(String description, EventSignificance significance) {
        this();
        this.legacyDescription = description;
        this.significance = significance;
    }

    public DiaryEntry(String description, EventSignificance significance, LocalDate eventDate) {
        this(description, significance);
        this.eventDate = eventDate;
    }

    public DiaryEntry(DiaryReference diaryReference, EventSignificance significance) {
        this();
        this.diaryReference = diaryReference;
        this.significance = significance;
    }

    public DiaryEntry(DiaryReference diaryReference, EventSignificance significance, LocalDate eventDate) {
        this(diaryReference, significance);
        this.eventDate = eventDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the description text. Returns the reference's description if available,
     * otherwise falls back to the legacy description for backward compatibility.
     */
    public String getDescription() {
        if (diaryReference != null) {
            return diaryReference.getDescription();
        }
        return legacyDescription;
    }

    /**
     * Sets the legacy description directly. For new code, prefer using setDiaryReference().
     */
    public void setDescription(String description) {
        this.legacyDescription = description;
    }

    public String getLegacyDescription() {
        return legacyDescription;
    }

    public void setLegacyDescription(String legacyDescription) {
        this.legacyDescription = legacyDescription;
    }

    public DiaryReference getDiaryReference() {
        return diaryReference;
    }

    public void setDiaryReference(DiaryReference diaryReference) {
        this.diaryReference = diaryReference;
    }

    /**
     * Checks if this entry needs migration from legacy description to reference.
     */
    @Transient
    public boolean needsMigration() {
        return diaryReference == null && legacyDescription != null && !legacyDescription.isEmpty();
    }

    public EventSignificance getSignificance() {
        return significance;
    }

    public void setSignificance(EventSignificance significance) {
        this.significance = significance;
    }

    public int getPoints() {
        return significance.getPoints();
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        DiaryEntry that = (DiaryEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
