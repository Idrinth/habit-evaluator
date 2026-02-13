package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;

import static org.junit.jupiter.api.Assertions.*;

class HabitAdapterTest {

    private List<Habit> habits;
    private TestClickListener clickListener;
    private HabitAdapter adapter;

    @BeforeEach
    void setUp() {
        habits = new ArrayList<>();
        clickListener = new TestClickListener();
        adapter = new HabitAdapter(habits, clickListener);
    }

    @Test
    void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testItemCountMatchesList() {
        habits.add(new Habit("Exercise", "Daily exercise"));
        habits.add(new Habit("Read", "Read a book"));
        habits.add(new Habit("Meditate", "Morning meditation"));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterAddingItem() {
        assertEquals(0, adapter.getItemCount());
        habits.add(new Habit("Exercise", "Daily exercise"));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testItemCountAfterRemovingItem() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
        habits.remove(habit);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testSetDisplayLanguage() {
        adapter.setDisplayLanguage("de");
        // Should not throw
    }

    @Test
    void testSetDisplayLanguageNull() {
        adapter.setDisplayLanguage(null);
        // Should not throw
    }

    @Test
    void testSetOnHabitEditListener() {
        adapter.setOnHabitEditListener(habit -> {});
        // Should not throw
    }

    @Test
    void testSetOnHabitDeleteListener() {
        adapter.setOnHabitDeleteListener(habit -> {});
        // Should not throw
    }

    @Test
    void testSetOnHabitEditListenerNull() {
        adapter.setOnHabitEditListener(null);
        // Should not throw
    }

    @Test
    void testSetOnHabitDeleteListenerNull() {
        adapter.setOnHabitDeleteListener(null);
        // Should not throw
    }

    @Test
    void testAdapterBackedByOriginalList() {
        habits.add(new Habit("A", "desc A"));
        assertEquals(1, adapter.getItemCount());
        habits.add(new Habit("B", "desc B"));
        assertEquals(2, adapter.getItemCount());
        habits.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void testManyHabitsItemCount() {
        for (int i = 0; i < 100; i++) {
            habits.add(new Habit("Habit " + i, "Description " + i));
        }
        assertEquals(100, adapter.getItemCount());
    }

    @Test
    void testHabitWithEntries() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setTargetFrequency(3);
        habit.setMaxEntriesPerDay(5);
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testHabitWithFrequencyType() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setFrequencyType(FrequencyType.DAILY);
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testHabitWithWeeklyFrequency() {
        Habit habit = new Habit("Exercise", "Weekly exercise");
        habit.setFrequencyType(FrequencyType.WEEKLY);
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testHabitWithMonthlyFrequency() {
        Habit habit = new Habit("Exercise", "Monthly exercise");
        habit.setFrequencyType(FrequencyType.MONTHLY);
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testHabitWithCategoryId() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setCategoryId("cat-health");
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testHabitWithNullCategoryId() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setCategoryId(null);
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void testSetEditAndDeleteListenersThenClear() {
        TestEditListener editListener = new TestEditListener();
        TestDeleteListener deleteListenerObj = new TestDeleteListener();
        adapter.setOnHabitEditListener(editListener);
        adapter.setOnHabitDeleteListener(deleteListenerObj);
        // Setting to null should not throw
        adapter.setOnHabitEditListener(null);
        adapter.setOnHabitDeleteListener(null);
    }

    @Test
    void testMultipleDisplayLanguageChanges() {
        adapter.setDisplayLanguage("en");
        adapter.setDisplayLanguage("de");
        adapter.setDisplayLanguage("es");
        adapter.setDisplayLanguage(null);
        // Should not throw at any point
    }

    private static class TestClickListener implements HabitAdapter.OnHabitClickListener {
        Habit lastClickedHabit;
        boolean deselected;

        @Override
        public void onHabitClick(Habit habit) {
            lastClickedHabit = habit;
        }

        @Override
        public void onHabitDeselect() {
            deselected = true;
        }
    }

    private static class TestEditListener implements HabitAdapter.OnHabitEditListener {
        Habit lastEdited;

        @Override
        public void onHabitEdit(Habit habit) {
            lastEdited = habit;
        }
    }

    private static class TestDeleteListener implements HabitAdapter.OnHabitDeleteListener {
        Habit lastDeleted;

        @Override
        public void onHabitDelete(Habit habit) {
            lastDeleted = habit;
        }
    }
}
