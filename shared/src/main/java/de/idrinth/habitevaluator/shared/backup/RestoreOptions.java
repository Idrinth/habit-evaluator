package de.idrinth.habitevaluator.shared.backup;

/**
 * Options controlling which model types to restore from a backup.
 * By default all types are included. Use the builder-style setters
 * or the static factory methods to customize.
 */
public class RestoreOptions {

    private boolean restoreCategories;
    private boolean restoreHabits;
    private boolean restoreDiaryEntries;
    private boolean restoreSleepEntries;
    private boolean restoreSportLogs;
    private boolean restoreFoodLogs;
    private boolean restoreEmotionData;
    private boolean restoreMeetingEntries;
    private boolean restoreReminderSettings;

    public RestoreOptions() {
        this.restoreCategories = true;
        this.restoreHabits = true;
        this.restoreDiaryEntries = true;
        this.restoreSleepEntries = true;
        this.restoreSportLogs = true;
        this.restoreFoodLogs = true;
        this.restoreEmotionData = true;
        this.restoreMeetingEntries = true;
        this.restoreReminderSettings = true;
    }

    /**
     * Returns options that include all model types (the default).
     */
    public static RestoreOptions all() {
        return new RestoreOptions();
    }

    /**
     * Returns options that exclude all model types.
     */
    public static RestoreOptions none() {
        RestoreOptions options = new RestoreOptions();
        options.restoreCategories = false;
        options.restoreHabits = false;
        options.restoreDiaryEntries = false;
        options.restoreSleepEntries = false;
        options.restoreSportLogs = false;
        options.restoreFoodLogs = false;
        options.restoreEmotionData = false;
        options.restoreMeetingEntries = false;
        options.restoreReminderSettings = false;
        return options;
    }

    public boolean isRestoreCategories() {
        return restoreCategories;
    }

    public void setRestoreCategories(boolean restoreCategories) {
        this.restoreCategories = restoreCategories;
    }

    public boolean isRestoreHabits() {
        return restoreHabits;
    }

    public void setRestoreHabits(boolean restoreHabits) {
        this.restoreHabits = restoreHabits;
    }

    public boolean isRestoreDiaryEntries() {
        return restoreDiaryEntries;
    }

    public void setRestoreDiaryEntries(boolean restoreDiaryEntries) {
        this.restoreDiaryEntries = restoreDiaryEntries;
    }

    public boolean isRestoreSleepEntries() {
        return restoreSleepEntries;
    }

    public void setRestoreSleepEntries(boolean restoreSleepEntries) {
        this.restoreSleepEntries = restoreSleepEntries;
    }

    public boolean isRestoreSportLogs() {
        return restoreSportLogs;
    }

    public void setRestoreSportLogs(boolean restoreSportLogs) {
        this.restoreSportLogs = restoreSportLogs;
    }

    public boolean isRestoreFoodLogs() {
        return restoreFoodLogs;
    }

    public void setRestoreFoodLogs(boolean restoreFoodLogs) {
        this.restoreFoodLogs = restoreFoodLogs;
    }

    public boolean isRestoreEmotionData() {
        return restoreEmotionData;
    }

    public void setRestoreEmotionData(boolean restoreEmotionData) {
        this.restoreEmotionData = restoreEmotionData;
    }

    public boolean isRestoreMeetingEntries() {
        return restoreMeetingEntries;
    }

    public void setRestoreMeetingEntries(boolean restoreMeetingEntries) {
        this.restoreMeetingEntries = restoreMeetingEntries;
    }

    public boolean isRestoreReminderSettings() {
        return restoreReminderSettings;
    }

    public void setRestoreReminderSettings(boolean restoreReminderSettings) {
        this.restoreReminderSettings = restoreReminderSettings;
    }
}
