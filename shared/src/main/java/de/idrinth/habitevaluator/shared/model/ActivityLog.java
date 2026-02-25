package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents an activity log entry tracking events with involved persons,
 * location, time range, and an optional activity description.
 */
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 1000, nullable = false)
    private String persons;

    @Column(nullable = false)
    private String location;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String activity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "activity_log_group_links",
            joinColumns = @JoinColumn(name = "activity_log_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_group_id")
    )
    private Set<ActivityGroup> groups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "activity_log_person_tags",
            joinColumns = @JoinColumn(name = "activity_log_id"),
            inverseJoinColumns = @JoinColumn(name = "person_tag_id")
    )
    private Set<PersonTag> personTags = new HashSet<>();

    public ActivityLog() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.date = LocalDate.now();
    }

    public ActivityLog(String persons, String location, LocalTime startTime, LocalTime endTime) {
        this();
        this.persons = persons;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ActivityLog(String persons, String location, LocalTime startTime, LocalTime endTime, LocalDate date) {
        this(persons, location, startTime, endTime);
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPersons() {
        return persons;
    }

    public void setPersons(String persons) {
        this.persons = persons;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
     * Calculates the activity duration in minutes from startTime and endTime.
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

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
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

    public Set<ActivityGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<ActivityGroup> groups) {
        this.groups = groups;
    }

    public Set<PersonTag> getPersonTags() {
        return personTags;
    }

    public void setPersonTags(Set<PersonTag> personTags) {
        this.personTags = personTags;
    }

    /**
     * Returns the persons as a parsed list of individual person names.
     */
    @Transient
    public List<String> getPersonList() {
        if (persons == null || persons.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(persons.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
