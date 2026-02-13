package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.Habit;

import static org.junit.jupiter.api.Assertions.*;

class HomeFragmentTest {

    @Test
    void testIsCategoryUsedWithMatchingCategory() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-1");
        habits.add(habit);

        assertTrue(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    void testIsCategoryUsedWithNoMatchingCategory() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-2");
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    void testIsCategoryUsedWithEmptyList() {
        assertFalse(HomeFragment.isCategoryUsed("cat-1", new ArrayList<>()));
    }

    @Test
    void testIsCategoryUsedWithNullList() {
        assertFalse(HomeFragment.isCategoryUsed("cat-1", null));
    }

    @Test
    void testIsCategoryUsedWithNullCategoryId() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-1");
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed(null, habits));
    }

    @Test
    void testIsCategoryUsedWithEmptyCategoryId() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        habit.setCategoryId("cat-1");
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed("", habits));
    }

    @Test
    void testIsCategoryUsedWithMultipleHabitsOneMatch() {
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
    void testIsCategoryUsedWithMultipleHabitsSameCategory() {
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
    void testIsCategoryUsedWithHabitWithNullCategoryId() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Test", "desc");
        // categoryId is null by default
        habits.add(habit);

        assertFalse(HomeFragment.isCategoryUsed("cat-1", habits));
    }

    @Test
    void testIsCategoryUsedNullCategoryAndNullList() {
        assertFalse(HomeFragment.isCategoryUsed(null, null));
    }

    @Test
    void testIsCategoryUsedEmptyCategoryAndNullList() {
        assertFalse(HomeFragment.isCategoryUsed("", null));
    }
}
