package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Per-user reminder/notification settings.
 * All reminders are disabled by default.
 */
@Entity
@Table(name = "reminder_settings")
public class ReminderSettings {

    @Id
    @Column(length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    /**
     * Whether to send a reminder after the configured wake-up time
     * to log the previous night's sleep data.
     */
    @Column(name = "sleep_reminder_enabled", nullable = false)
    private boolean sleepReminderEnabled;

    /**
     * Time at which the sleep data reminder fires (e.g. shortly after typical wake-up).
     */
    @Column(name = "sleep_reminder_time")
    private LocalTime sleepReminderTime;

    /**
     * Whether to send an evening reminder if no diary entry has been recorded today.
     */
    @Column(name = "diary_reminder_enabled", nullable = false)
    private boolean diaryReminderEnabled;

    /**
     * Time in the evening at which the diary reminder fires.
     */
    @Column(name = "diary_reminder_time")
    private LocalTime diaryReminderTime;

    /**
     * Whether to send random emotion-check reminders during waking hours.
     */
    @Column(name = "emotion_reminder_enabled", nullable = false)
    private boolean emotionReminderEnabled;

    /**
     * How many random emotion-check reminders to send per day.
     */
    @Column(name = "emotion_reminder_count", nullable = false)
    private int emotionReminderCount;

    /**
     * Whether to send a daily reminder to record gratitude entries.
     */
    @Column(name = "gratitude_reminder_enabled", nullable = false)
    private boolean gratitudeReminderEnabled;

    /**
     * Time at which the gratitude reminder fires (e.g. evening).
     */
    @Column(name = "gratitude_reminder_time")
    private LocalTime gratitudeReminderTime;

    /**
     * Start of the user's waking hours window (used for emotion reminders
     * and as earliest possible time for any reminder).
     */
    @Column(name = "waking_hours_start")
    private LocalTime wakingHoursStart;

    /**
     * End of the user's waking hours window (used for emotion reminders
     * and as latest possible time for any reminder).
     */
    @Column(name = "waking_hours_end")
    private LocalTime wakingHoursEnd;

    public ReminderSettings() {
        this.id = UUID.randomUUID().toString();
        this.sleepReminderEnabled = false;
        this.sleepReminderTime = LocalTime.of(8, 0);
        this.diaryReminderEnabled = false;
        this.diaryReminderTime = LocalTime.of(20, 0);
        this.emotionReminderEnabled = false;
        this.emotionReminderCount = 3;
        this.gratitudeReminderEnabled = false;
        this.gratitudeReminderTime = LocalTime.of(8, 0);
        this.wakingHoursStart = LocalTime.of(7, 0);
        this.wakingHoursEnd = LocalTime.of(22, 0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSleepReminderEnabled() {
        return sleepReminderEnabled;
    }

    public void setSleepReminderEnabled(boolean sleepReminderEnabled) {
        this.sleepReminderEnabled = sleepReminderEnabled;
    }

    public LocalTime getSleepReminderTime() {
        return sleepReminderTime;
    }

    public void setSleepReminderTime(LocalTime sleepReminderTime) {
        this.sleepReminderTime = sleepReminderTime;
    }

    public boolean isDiaryReminderEnabled() {
        return diaryReminderEnabled;
    }

    public void setDiaryReminderEnabled(boolean diaryReminderEnabled) {
        this.diaryReminderEnabled = diaryReminderEnabled;
    }

    public LocalTime getDiaryReminderTime() {
        return diaryReminderTime;
    }

    public void setDiaryReminderTime(LocalTime diaryReminderTime) {
        this.diaryReminderTime = diaryReminderTime;
    }

    public boolean isEmotionReminderEnabled() {
        return emotionReminderEnabled;
    }

    public void setEmotionReminderEnabled(boolean emotionReminderEnabled) {
        this.emotionReminderEnabled = emotionReminderEnabled;
    }

    public int getEmotionReminderCount() {
        return emotionReminderCount;
    }

    public void setEmotionReminderCount(int emotionReminderCount) {
        this.emotionReminderCount = Math.max(1, Math.min(10, emotionReminderCount));
    }

    public boolean isGratitudeReminderEnabled() {
        return gratitudeReminderEnabled;
    }

    public void setGratitudeReminderEnabled(boolean gratitudeReminderEnabled) {
        this.gratitudeReminderEnabled = gratitudeReminderEnabled;
    }

    public LocalTime getGratitudeReminderTime() {
        return gratitudeReminderTime;
    }

    public void setGratitudeReminderTime(LocalTime gratitudeReminderTime) {
        this.gratitudeReminderTime = gratitudeReminderTime;
    }

    public LocalTime getWakingHoursStart() {
        return wakingHoursStart;
    }

    public void setWakingHoursStart(LocalTime wakingHoursStart) {
        this.wakingHoursStart = wakingHoursStart;
    }

    public LocalTime getWakingHoursEnd() {
        return wakingHoursEnd;
    }

    public void setWakingHoursEnd(LocalTime wakingHoursEnd) {
        this.wakingHoursEnd = wakingHoursEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReminderSettings that = (ReminderSettings) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
