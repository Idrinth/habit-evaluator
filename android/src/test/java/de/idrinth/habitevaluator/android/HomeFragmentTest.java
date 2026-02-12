package de.idrinth.habitevaluator.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.Habit;

import static org.junit.Assert.*;

public class HomeFragmentTest {

    @Test
    public void testIsCategoryUsedWithMatchingCategory() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-1");
        habits.add(habit);

        assertTrue(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    public void testIsCategoryUsedWithNoMatchingCategory() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-2");
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    public void testIsCategoryUsedWithEmptyList() {
        assertFalse(HomeFragment.isCategoryUsed("cat-1", new ArrayList<>()));
    }

    @Test
    public void testIsCategoryUsedWithNullList() {
        assertFalse(HomeFragment.isCategoryUsed("cat-1", null));
    }

    @Test
    public void testIsCategoryUsedWithNullCategoryId() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-1");
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed(null, habits));
    }

    @Test
    public void testIsCategoryUsedWithEmptyCategoryId() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-1");
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed("", habits));
    }

    @Test
    public void testIsCategoryUsedWithMultipleHabitsOneMatch() {
        List<Habit> habits = new ArrayList<>();
        Habit habit1 = new Habit("Test1", "desc1");
        habit1.setCategoryId("cat-1");
        Habit habit2 = new Habit("Test2", "desc2");
        habit2.setCategoryId("cat-2");
        Habit habit3 = new Habit("Test3", "desc3");
        habit3.setCategoryId("cat-3");
        habits.add(habit1);
        habits.add(habit2);
        habits.add(habit3);

        assertTrue(HomeFragment.isCategoryUsed("cat-2", habits));
    }

    @Test
    public void testIsCategoryUsedWithMultipleHabitsSameCategory() {
        List<Habit> habits = new ArrayList<>();
        Habit habit1 = new Habit("Test1", "desc1");
        habit1.setCategoryId("cat-1");
        Habit habit2 = new Habit("Test2", "desc2");
        habit2.setCategoryId("cat-1");
        habits.add(habit1);
        habits.add(habit2);

        assertTrue(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    public void testIsCategoryUsedWithHabitWithNullCategoryId() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        // categoryId is null by default
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    public void testIsCategoryUsedNullCategoryAndNullList() {
        assertFalse(HomeFragment.isCategoryUsed(null, null));
    }

    @Test
    public void testIsCategoryUsedEmptyCategoryAndNullList() {
        assertFalse(HomeFragment.isCategoryUsed("", null));
    }
}
