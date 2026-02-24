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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a time slot in the weekly planner.
 * Each slot assigns one or more groups to a specific day-of-week, starting hour, and duration.
 * Day of week uses ISO-8601: 1 = Monday through 7 = Sunday.
 * Hour ranges from 0 to 23. Duration is in hours (1 or more).
 */
@Entity
@Table(name = "week_planner_slots")
public class WeekPlannerSlot {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private int duration;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "week_planner_slot_group_links",
            joinColumns = @JoinColumn(name = "slot_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<PlannerGroup> groups = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public WeekPlannerSlot() {
        this.id = UUID.randomUUID().toString();
        this.duration = 1;
    }

    public WeekPlannerSlot(int dayOfWeek, int hour) {
        this();
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
    }

    public WeekPlannerSlot(int dayOfWeek, int hour, int duration) {
        this();
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns whether this slot covers the given hour.
     * A slot starting at hour H with duration D covers hours H through H+D-1.
     */
    public boolean coversHour(int queryHour) {
        return queryHour >= hour && queryHour < hour + duration;
    }

    public Set<PlannerGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<PlannerGroup> groups) {
        this.groups = groups;
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
        WeekPlannerSlot that = (WeekPlannerSlot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
