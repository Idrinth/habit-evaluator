package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.shared.model.Habit;

import static org.junit.jupiter.api.Assertions.*;

class PointDevelopmentFragmentTest {

    @Test
    void testFindHabitByIdWithMatchingHabit() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Exercise", "Daily exercise");
        String id = habit.getId();
        habits.add(habit);

        assertSame(habit, PointDevelopmentFragment.findHabitById(habits, id));
    }

    @Test
    void testFindHabitByIdWithNoMatch() {
        List<Habit> habits = new ArrayList<>();
        Habit habit = new Habit("Exercise", "Daily exercise");
        habits.add(habit);

        assertNull(PointDevelopmentFragment.findHabitById(habits, "non-existent-id"));
    }

    @Test
    void testFindHabitByIdWithNullList() {
        assertNull(PointDevelopmentFragment.findHabitById(null, "some-id"));
    }

    @Test
    void testFindHabitByIdWithNullId() {
        List<Habit> habits = new ArrayList<>();
        habits.add(new Habit("Exercise", "Daily exercise"));

        assertNull(PointDevelopmentFragment.findHabitById(habits, null));
    }

    @Test
    void testFindHabitByIdWithBothNull() {
        assertNull(PointDevelopmentFragment.findHabitById(null, null));
    }

    @Test
    void testFindHabitByIdWithEmptyList() {
        assertNull(PointDevelopmentFragment.findHabitById(new ArrayList<>(), "some-id"));
    }

    @Test
    void testFindHabitByIdWithMultipleHabitsReturnsCorrectOne() {
        List<Habit> habits = new ArrayList<>();
        Habit habit1 = new Habit("Exercise", "Daily exercise");
        Habit habit2 = new Habit("Reading", "Read 30 min");
        Habit habit3 = new Habit("Meditation", "Morning meditation");
        habits.add(habit1);
        habits.add(habit2);
        habits.add(habit3);

        assertSame(habit2, PointDevelopmentFragment.findHabitById(habits, habit2.getId()));
    }

    @Test
    void testFindHabitByIdReturnsFirstMatchWhenDuplicateIds() {
        List<Habit> habits = new ArrayList<>();
        Habit habit1 = new Habit("Exercise", "v1");
        Habit habit2 = new Habit("Reading", "v2");
        habits.add(habit1);
        habits.add(habit2);

        Habit result = PointDevelopmentFragment.findHabitById(habits, habit1.getId());
        assertSame(habit1, result);
    }
}
