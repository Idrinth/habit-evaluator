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
 * Represents a time slot in the weekly planner.
 * Each slot assigns a group to a specific day-of-week and hour.
 * Day of week uses ISO-8601: 1 = Monday through 7 = Sunday.
 * Hour ranges from 0 to 23.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private PlannerGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public WeekPlannerSlot() {
        this.id = UUID.randomUUID().toString();
    }

    public WeekPlannerSlot(int dayOfWeek, int hour) {
        this();
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
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

    public PlannerGroup getGroup() {
        return group;
    }

    public void setGroup(PlannerGroup group) {
        this.group = group;
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
