package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single sleep tracking entry with a from-time and until-time.
 * Hours slept are calculated from the time range.
 */
@Entity
@Table(name = "sleep_entries")
public class SleepEntry {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @Column(name = "until_time", nullable = false)
    private LocalTime untilTime;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public SleepEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.date = LocalDate.now();
    }

    public SleepEntry(LocalTime fromTime, LocalTime untilTime) {
        this();
        this.fromTime = fromTime;
        this.untilTime = untilTime;
    }

    public SleepEntry(LocalTime fromTime, LocalTime untilTime, LocalDate date) {
        this();
        this.fromTime = fromTime;
        this.untilTime = untilTime;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
    }

    public LocalTime getUntilTime() {
        return untilTime;
    }

    public void setUntilTime(LocalTime untilTime) {
        this.untilTime = untilTime;
    }

    /**
     * Calculates hours slept from the time range.
     * If untilTime is before fromTime, it is assumed to cross midnight.
     */
    public double getHours() {
        if (fromTime == null || untilTime == null) {
            return 0;
        }
        int fromMinutes = fromTime.getHour() * 60 + fromTime.getMinute();
        int untilMinutes = untilTime.getHour() * 60 + untilTime.getMinute();
        int diff = untilMinutes - fromMinutes;
        if (diff <= 0) {
            diff += 24 * 60;
        }
        return diff / 60.0;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        SleepEntry that = (SleepEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
