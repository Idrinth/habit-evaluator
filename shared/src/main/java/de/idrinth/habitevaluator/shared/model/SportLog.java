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
 * Represents a single sport activity log entry with a user-definable measurement,
 * start time, and end time. Duration is calculated from the time range.
 */
@Entity
@Table(name = "sport_logs")
public class SportLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double measurement;

    @Column(name = "measurement_unit", nullable = false)
    private String measurementUnit;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public SportLog() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.date = LocalDate.now();
    }

    public SportLog(String name, double measurement, String measurementUnit,
                    LocalTime startTime, LocalTime endTime) {
        this();
        this.name = name;
        this.measurement = measurement;
        this.measurementUnit = measurementUnit;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SportLog(String name, double measurement, String measurementUnit,
                    LocalTime startTime, LocalTime endTime, LocalDate date) {
        this();
        this.name = name;
        this.measurement = measurement;
        this.measurementUnit = measurementUnit;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
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

    public double getMeasurement() {
        return measurement;
    }

    public void setMeasurement(double measurement) {
        this.measurement = measurement;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
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
     * Calculates the duration of the sport activity in hours.
     * If endTime is before startTime, it is assumed to cross midnight.
     */
    public double getDurationHours() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        int startMinutes = startTime.getHour() * 60 + startTime.getMinute();
        int endMinutes = endTime.getHour() * 60 + endTime.getMinute();
        int diff = endMinutes - startMinutes;
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
        SportLog that = (SportLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
