package de.idrinth.habitevaluator.android.ui;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.Habit;

import static org.junit.Assert.*;

public class HabitAdapterTest {

    private List<Habit> habits;
    private TestClickListener clickListener;
    private HabitAdapter adapter;

    @Before
    public void setUp() {
        habits = new ArrayList<>();
        clickListener = new TestClickListener();
        adapter = new HabitAdapter(habits, clickListener);
    }

    @Test
    public void testEmptyAdapterItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCountMatchesList() {
        habits.add(new Habit("Exercise", "Daily exercise"));
        habits.add(new Habit("Read", "Read a book"));
        habits.add(new Habit("Meditate", "Morning meditation"));
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterAddingItem() {
        assertEquals(0, adapter.getItemCount());
        habits.add(new Habit("Exercise", "Daily exercise"));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testItemCountAfterRemovingItem() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habits.add(habit);
        assertEquals(1, adapter.getItemCount());
        habits.remove(habit);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testSetDisplayLanguage() {
        adapter.setDisplayLanguage("de");
        // Should not throw
    }

    @Test
    public void testSetDisplayLanguageNull() {
        adapter.setDisplayLanguage(null);
        // Should not throw
    }

    @Test
    public void testSetOnHabitEditListener() {
        adapter.setOnHabitEditListener(habit -> {});
        // Should not throw
    }

    @Test
    public void testSetOnHabitDeleteListener() {
        adapter.setOnHabitDeleteListener(habit -> {});
        // Should not throw
    }

    @Test
    public void testSetOnHabitEditListenerNull() {
        adapter.setOnHabitEditListener(null);
        // Should not throw
    }

    @Test
    public void testSetOnHabitDeleteListenerNull() {
        adapter.setOnHabitDeleteListener(null);
        // Should not throw
    }

    @Test
    public void testAdapterBackedByOriginalList() {
        habits.add(new Habit("A", "desc A"));
        assertEquals(1, adapter.getItemCount());
        habits.add(new Habit("B", "desc B"));
        assertEquals(2, adapter.getItemCount());
        habits.clear();
        assertEquals(0, adapter.getItemCount());
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
}
