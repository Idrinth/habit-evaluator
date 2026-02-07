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
        this.categoriesAdded = categoriesAdded;
        this.habitsAdded = habitsAdded;
        this.habitsMerged = habitsMerged;
        this.entriesAdded = entriesAdded;
        this.diaryEntriesAdded = diaryEntriesAdded;
        this.sleepEntriesAdded = sleepEntriesAdded;
        this.sportLogsAdded = sportLogsAdded;
        this.foodLogsAdded = foodLogsAdded;
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

    public int getTotalChanges() {
        return categoriesAdded + habitsAdded + habitsMerged + diaryEntriesAdded
                + sleepEntriesAdded + sportLogsAdded + foodLogsAdded;
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
                + '}';
    }
}
