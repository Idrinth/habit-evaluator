package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a meeting log entry tracking person interactions.
 * Stores the place, attendants, and time range of the meeting.
 */
@Entity
@Table(name = "meeting_entries")
public class MeetingEntry {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String place;

    @Column(length = 1000, nullable = false)
    private String attendants;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public MeetingEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.date = LocalDate.now();
    }

    public MeetingEntry(String place, String attendants, LocalTime startTime, LocalTime endTime) {
        this();
        this.place = place;
        this.attendants = attendants;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public MeetingEntry(String place, String attendants, LocalTime startTime, LocalTime endTime, LocalDate date) {
        this(place, attendants, startTime, endTime);
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getAttendants() {
        return attendants;
    }

    public void setAttendants(String attendants) {
        this.attendants = attendants;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Calculates the meeting duration in minutes from startTime and endTime.
     * Handles midnight crossing (e.g., 23:00 to 01:00 = 120 minutes).
     */
    @Transient
    public Integer getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return null;
        }
        int startMinutes = startTime.getHour() * 60 + startTime.getMinute();
        int endMinutes = endTime.getHour() * 60 + endTime.getMinute();
        int diff = endMinutes - startMinutes;
        if (diff <= 0) {
            diff += 24 * 60;
        }
        return diff;
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
        MeetingEntry that = (MeetingEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
