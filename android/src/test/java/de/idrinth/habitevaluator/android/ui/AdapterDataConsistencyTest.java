package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SportLog;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: every item in the adapter's backing data must be represented
 * in getItemCount(). If getItemCount() returns fewer items than the data holds,
 * the RecyclerView will silently drop rows and the user cannot scroll to them.
 */
class AdapterDataConsistencyTest {

    // ── HabitAdapter ────────────────────────────────────────────────────────

    @Test
    void testHabitAdapterItemCountAlwaysMatchesBackingList() {
        List<Habit> habits = new ArrayList<>();
        HabitAdapter adapter = new HabitAdapter(habits, new NoOpHabitClickListener());

        for (int i = 0; i < 50; i++) {
            habits.add(new Habit("Habit " + i, "desc"));
            assertEquals(
                    habits.size(), adapter.getItemCount()
            ,
                    "After adding item " + i + ", getItemCount must equal list size");
        }

        while (!habits.isEmpty()) {
            habits.remove(habits.size() - 1);
            assertEquals(
                    habits.size(), adapter.getItemCount()
            ,
                    "After removing, getItemCount must equal list size");
        }
    }

    @Test
    void testHabitAdapterItemCountAfterBulkClear() {
        List<Habit> habits = new ArrayList<>();
        HabitAdapter adapter = new HabitAdapter(habits, new NoOpHabitClickListener());

        for (int i = 0; i < 20; i++) {
            habits.add(new Habit("H" + i, "d"));
        }
        assertEquals(20, adapter.getItemCount());

        habits.clear();
        assertEquals(0, adapter.getItemCount());
    }

    // ── DiaryEntryAdapter ───────────────────────────────────────────────────

    @Test
    void testDiaryEntryAdapterItemCountAlwaysMatchesBackingList() {
        List<DiaryEntry> entries = new ArrayList<>();
        DiaryEntryAdapter adapter = new DiaryEntryAdapter(entries, null);

        for (int i = 0; i < 50; i++) {
            DiaryEntry entry = new DiaryEntry();
            entry.setDescription("Entry " + i);
            entry.setSignificance(EventSignificance.NORMAL);
            entry.setEventDate(LocalDate.now());
            entries.add(entry);
            assertEquals(entries.size(), adapter.getItemCount());
        }

        while (!entries.isEmpty()) {
            entries.remove(entries.size() - 1);
            assertEquals(entries.size(), adapter.getItemCount());
        }
    }

    // ── SleepEntryAdapter ───────────────────────────────────────────────────

    @Test
    void testSleepEntryAdapterItemCountAlwaysMatchesBackingList() {
        List<SleepEntry> entries = new ArrayList<>();
        SleepEntryAdapter adapter = new SleepEntryAdapter(entries, null);

        for (int i = 0; i < 50; i++) {
            SleepEntry entry = new SleepEntry();
            entry.setDate(LocalDate.now().minusDays(i));
            entry.setFromTime(LocalTime.of(23, 0));
            entry.setUntilTime(LocalTime.of(7, 0));
            entries.add(entry);
            assertEquals(entries.size(), adapter.getItemCount());
        }

        while (!entries.isEmpty()) {
            entries.remove(entries.size() - 1);
            assertEquals(entries.size(), adapter.getItemCount());
        }
    }

    // ── EmotionPairAdapter ──────────────────────────────────────────────────

    @Test
    void testEmotionPairAdapterItemCountAlwaysMatchesBackingList() {
        List<EmotionPair> pairs = new ArrayList<>();
        EmotionPairAdapter adapter = new EmotionPairAdapter(pairs, null, null);

        for (int i = 0; i < 30; i++) {
            EmotionPair pair = new EmotionPair();
            pair.setNegativeLabel("neg" + i);
            pair.setPositiveLabel("pos" + i);
            pairs.add(pair);
            assertEquals(pairs.size(), adapter.getItemCount());
        }

        while (!pairs.isEmpty()) {
            pairs.remove(pairs.size() - 1);
            assertEquals(pairs.size(), adapter.getItemCount());
        }
    }

    // ── EmotionDataAdapter (expandable groups) ──────────────────────────────
    // Note: EmotionDataAdapter.setData() calls notifyDataSetChanged() which
    // requires the Android RecyclerView runtime. These tests verify the adapter's
    // initial empty state — the contract that getItemCount() == 0 before any
    // data is loaded, preventing IndexOutOfBoundsException on first layout pass.

    @Test
    void testEmotionDataAdapterStartsEmpty() {
        EmotionDataAdapter adapter = new EmotionDataAdapter(pair -> {}, pair -> {}, entry -> {});
        assertEquals(
                0, adapter.getItemCount()
        ,
                "EmotionDataAdapter must start with zero items before setData is called");
    }

    @Test
    void testEmotionDataAdapterWithNullListenersStartsEmpty() {
        EmotionDataAdapter adapter = new EmotionDataAdapter(null, null, null);
        assertEquals(0, adapter.getItemCount());
    }

    // ── EditHabitAdapter ────────────────────────────────────────────────────

    @Test
    void testEditHabitAdapterItemCountMatchesBackingList() {
        List<Habit> habits = new ArrayList<>();
        List<HabitCategory> categories = new ArrayList<>();
        HabitCategory cat = new HabitCategory("Health", "Health category", "#00FF00");
        categories.add(cat);

        for (int i = 0; i < 20; i++) {
            Habit h = new Habit("Habit " + i, "desc " + i);
            h.setCategoryId(cat.getId());
            habits.add(h);
        }

        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(habits.size(), adapter.getItemCount());
    }

    @Test
    void testEditHabitAdapterEmptyList() {
        List<Habit> habits = new ArrayList<>();
        List<HabitCategory> categories = new ArrayList<>();
        EditHabitAdapter adapter = new EditHabitAdapter(habits, false, categories, "en");
        assertEquals(0, adapter.getItemCount());
    }

    // ── SportLogAdapter ─────────────────────────────────────────────────────

    @Test
    void testSportLogAdapterItemCountAlwaysMatchesBackingList() {
        List<SportLog> entries = new ArrayList<>();
        SportLogAdapter adapter = new SportLogAdapter(entries, null);

        for (int i = 0; i < 30; i++) {
            entries.add(new SportLog("Running", 5.0, "km",
                    LocalTime.of(8, 0), LocalTime.of(9, 0)));
            assertEquals(entries.size(), adapter.getItemCount());
        }

        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    // ── FoodLogAdapter ──────────────────────────────────────────────────────

    @Test
    void testFoodLogAdapterItemCountAlwaysMatchesBackingList() {
        List<FoodLog> entries = new ArrayList<>();
        FoodLogAdapter adapter = new FoodLogAdapter(entries, null);

        for (int i = 0; i < 30; i++) {
            entries.add(new FoodLog(10.0, 200, LocalDateTime.now(), "Food " + i));
            assertEquals(entries.size(), adapter.getItemCount());
        }

        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    // ── MedicationLogAdapter ────────────────────────────────────────────────

    @Test
    void testMedicationLogAdapterItemCountAlwaysMatchesBackingList() {
        List<MedicationLog> entries = new ArrayList<>();
        MedicationLogAdapter adapter = new MedicationLogAdapter(entries, null);

        for (int i = 0; i < 30; i++) {
            entries.add(new MedicationLog(null, 1.0, LocalDateTime.now()));
            assertEquals(entries.size(), adapter.getItemCount());
        }

        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    // ── ActivityLogAdapter ────────────────────────────────────────────────────

    @Test
    void testActivityLogAdapterItemCountAlwaysMatchesBackingList() {
        List<ActivityLog> entries = new ArrayList<>();
        ActivityLogAdapter adapter = new ActivityLogAdapter(entries, null);

        for (int i = 0; i < 30; i++) {
            entries.add(new ActivityLog("Person " + i, "Location " + i,
                    LocalTime.of(10, 0), LocalTime.of(11, 0)));
            assertEquals(entries.size(), adapter.getItemCount());
        }

        entries.clear();
        assertEquals(0, adapter.getItemCount());
    }

    // ── Cross-adapter: bulk insert + remove never desynchronizes ────────────

    @Test
    void testAllSimpleAdaptersStaySynchronizedDuringMixedOperations() {
        // HabitAdapter
        List<Habit> habits = new ArrayList<>();
        HabitAdapter habitAdapter = new HabitAdapter(habits, new NoOpHabitClickListener());

        // SleepEntryAdapter
        List<SleepEntry> sleeps = new ArrayList<>();
        SleepEntryAdapter sleepAdapter = new SleepEntryAdapter(sleeps, null);

        // DiaryEntryAdapter
        List<DiaryEntry> diaries = new ArrayList<>();
        DiaryEntryAdapter diaryAdapter = new DiaryEntryAdapter(diaries, null);

        // Add items to all three
        for (int i = 0; i < 10; i++) {
            habits.add(new Habit("H" + i, "d"));
            SleepEntry se = new SleepEntry();
            se.setDate(LocalDate.now().minusDays(i));
            se.setFromTime(LocalTime.of(23, 0));
            se.setUntilTime(LocalTime.of(7, 0));
            sleeps.add(se);
            DiaryEntry de = new DiaryEntry();
            de.setDescription("D" + i);
            de.setSignificance(EventSignificance.MINOR);
            de.setEventDate(LocalDate.now());
            diaries.add(de);
        }

        assertEquals(10, habitAdapter.getItemCount());
        assertEquals(10, sleepAdapter.getItemCount());
        assertEquals(10, diaryAdapter.getItemCount());

        // Remove every other item
        for (int i = 9; i >= 0; i -= 2) {
            habits.remove(i);
            sleeps.remove(i);
            diaries.remove(i);
        }

        assertEquals(habits.size(), habitAdapter.getItemCount());
        assertEquals(sleeps.size(), sleepAdapter.getItemCount());
        assertEquals(diaries.size(), diaryAdapter.getItemCount());
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private static class NoOpHabitClickListener implements HabitAdapter.OnHabitClickListener {
        @Override
        public void onHabitClick(Habit habit) {}

        @Override
        public void onHabitDeselect() {}
    }
}
