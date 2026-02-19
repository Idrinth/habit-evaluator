package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Per-user module visibility settings.
 * Controls which modules are shown in navigation.
 * All modules are visible by default.
 */
@Entity
@Table(name = "module_visibility")
public class ModuleVisibility {

    @Id
    @Column(length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "diary_visible", nullable = false)
    private boolean diaryVisible;

    @Column(name = "sleep_visible", nullable = false)
    private boolean sleepVisible;

    @Column(name = "emotions_visible", nullable = false)
    private boolean emotionsVisible;

    @Column(name = "points_visible", nullable = false)
    private boolean pointsVisible;

    @Column(name = "statistics_visible", nullable = false)
    private boolean statisticsVisible;

    @Column(name = "food_log_visible", nullable = false)
    private boolean foodLogVisible;

    @Column(name = "sport_log_visible", nullable = false)
    private boolean sportLogVisible;

    @Column(name = "medication_visible", nullable = false)
    private boolean medicationVisible;

    @Column(name = "backup_visible", nullable = false)
    private boolean backupVisible;

    @Column(name = "pdf_export_visible", nullable = false)
    private boolean pdfExportVisible;

    @Column(name = "activity_log_visible", nullable = false)
    private boolean activityLogVisible;

    public ModuleVisibility() {
        this.id = UUID.randomUUID().toString();
        this.diaryVisible = true;
        this.sleepVisible = true;
        this.emotionsVisible = true;
        this.pointsVisible = true;
        this.statisticsVisible = true;
        this.foodLogVisible = true;
        this.sportLogVisible = true;
        this.medicationVisible = true;
        this.backupVisible = true;
        this.pdfExportVisible = true;
        this.activityLogVisible = true;
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

    public boolean isDiaryVisible() {
        return diaryVisible;
    }

    public void setDiaryVisible(boolean diaryVisible) {
        this.diaryVisible = diaryVisible;
    }

    public boolean isSleepVisible() {
        return sleepVisible;
    }

    public void setSleepVisible(boolean sleepVisible) {
        this.sleepVisible = sleepVisible;
    }

    public boolean isEmotionsVisible() {
        return emotionsVisible;
    }

    public void setEmotionsVisible(boolean emotionsVisible) {
        this.emotionsVisible = emotionsVisible;
    }

    public boolean isPointsVisible() {
        return pointsVisible;
    }

    public void setPointsVisible(boolean pointsVisible) {
        this.pointsVisible = pointsVisible;
    }

    public boolean isStatisticsVisible() {
        return statisticsVisible;
    }

    public void setStatisticsVisible(boolean statisticsVisible) {
        this.statisticsVisible = statisticsVisible;
    }

    public boolean isFoodLogVisible() {
        return foodLogVisible;
    }

    public void setFoodLogVisible(boolean foodLogVisible) {
        this.foodLogVisible = foodLogVisible;
    }

    public boolean isSportLogVisible() {
        return sportLogVisible;
    }

    public void setSportLogVisible(boolean sportLogVisible) {
        this.sportLogVisible = sportLogVisible;
    }

    public boolean isMedicationVisible() {
        return medicationVisible;
    }

    public void setMedicationVisible(boolean medicationVisible) {
        this.medicationVisible = medicationVisible;
    }

    public boolean isBackupVisible() {
        return backupVisible;
    }

    public void setBackupVisible(boolean backupVisible) {
        this.backupVisible = backupVisible;
    }

    public boolean isPdfExportVisible() {
        return pdfExportVisible;
    }

    public void setPdfExportVisible(boolean pdfExportVisible) {
        this.pdfExportVisible = pdfExportVisible;
    }

    public boolean isActivityLogVisible() {
        return activityLogVisible;
    }

    public void setActivityLogVisible(boolean activityLogVisible) {
        this.activityLogVisible = activityLogVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleVisibility that = (ModuleVisibility) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
