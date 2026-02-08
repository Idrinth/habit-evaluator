package de.idrinth.habitevaluator.shared.backup;

/**
 * Holds the results of a backup merge operation, summarizing what was added or merged.
 */
public class MergeResult {

    private final int categoriesAdded;
    private final int habitsAdded;
    private final int habitsMerged;
    private final int entriesAdded;
    private final int diaryEntriesAdded;
    private final int sleepEntriesAdded;
    private final int sportLogsAdded;
    private final int foodLogsAdded;
    private final int emotionPairsAdded;
    private final int emotionEntriesAdded;
    private final boolean reminderSettingsRestored;

    public MergeResult(int categoriesAdded, int habitsAdded, int habitsMerged,
                       int entriesAdded, int diaryEntriesAdded, int sleepEntriesAdded) {
        this(categoriesAdded, habitsAdded, habitsMerged, entriesAdded,
                diaryEntriesAdded, sleepEntriesAdded, 0);
    }

    public MergeResult(int categoriesAdded, int habitsAdded, int habitsMerged,
                       int entriesAdded, int diaryEntriesAdded, int sleepEntriesAdded,
                       int sportLogsAdded) {
        this(categoriesAdded, habitsAdded, habitsMerged, entriesAdded,
                diaryEntriesAdded, sleepEntriesAdded, sportLogsAdded, 0);
    }

    public MergeResult(int categoriesAdded, int habitsAdded, int habitsMerged,
                       int entriesAdded, int diaryEntriesAdded, int sleepEntriesAdded,
                       int sportLogsAdded, int foodLogsAdded) {
        this(categoriesAdded, habitsAdded, habitsMerged, entriesAdded,
                diaryEntriesAdded, sleepEntriesAdded, sportLogsAdded, foodLogsAdded,
                0, 0, false);
    }

    public MergeResult(int categoriesAdded, int habitsAdded, int habitsMerged,
                       int entriesAdded, int diaryEntriesAdded, int sleepEntriesAdded,
                       int sportLogsAdded, int foodLogsAdded,
                       int emotionPairsAdded, int emotionEntriesAdded,
                       boolean reminderSettingsRestored) {
        this.categoriesAdded = categoriesAdded;
        this.habitsAdded = habitsAdded;
        this.habitsMerged = habitsMerged;
        this.entriesAdded = entriesAdded;
        this.diaryEntriesAdded = diaryEntriesAdded;
        this.sleepEntriesAdded = sleepEntriesAdded;
        this.sportLogsAdded = sportLogsAdded;
        this.foodLogsAdded = foodLogsAdded;
        this.emotionPairsAdded = emotionPairsAdded;
        this.emotionEntriesAdded = emotionEntriesAdded;
        this.reminderSettingsRestored = reminderSettingsRestored;
    }

    public int getCategoriesAdded() {
        return categoriesAdded;
    }

    public int getHabitsAdded() {
        return habitsAdded;
    }

    public int getHabitsMerged() {
        return habitsMerged;
    }

    public int getEntriesAdded() {
        return entriesAdded;
    }

    public int getDiaryEntriesAdded() {
        return diaryEntriesAdded;
    }

    public int getSleepEntriesAdded() {
        return sleepEntriesAdded;
    }

    public int getSportLogsAdded() {
        return sportLogsAdded;
    }

    public int getFoodLogsAdded() {
        return foodLogsAdded;
    }

    public int getEmotionPairsAdded() {
        return emotionPairsAdded;
    }

    public int getEmotionEntriesAdded() {
        return emotionEntriesAdded;
    }

    public boolean isReminderSettingsRestored() {
        return reminderSettingsRestored;
    }

    public int getTotalChanges() {
        return categoriesAdded + habitsAdded + habitsMerged + diaryEntriesAdded
                + sleepEntriesAdded + sportLogsAdded + foodLogsAdded
                + emotionPairsAdded + emotionEntriesAdded
                + (reminderSettingsRestored ? 1 : 0);
    }

    @Override
    public String toString() {
        return "MergeResult{"
                + "categories added=" + categoriesAdded
                + ", habits added=" + habitsAdded
                + ", habits merged=" + habitsMerged
                + ", entries added=" + entriesAdded
                + ", diary entries added=" + diaryEntriesAdded
                + ", sleep entries added=" + sleepEntriesAdded
                + ", sport logs added=" + sportLogsAdded
                + ", food logs added=" + foodLogsAdded
                + ", emotion pairs added=" + emotionPairsAdded
                + ", emotion entries added=" + emotionEntriesAdded
                + ", reminder settings restored=" + reminderSettingsRestored
                + '}';
    }
}
