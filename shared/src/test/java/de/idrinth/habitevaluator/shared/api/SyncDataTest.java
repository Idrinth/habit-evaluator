package de.idrinth.habitevaluator.shared.api;

import de.idrinth.habitevaluator.shared.model.Habit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SyncDataTest {

    @Test
    void testDefaultConstructor() {
        SyncData data = new SyncData();
        assertNotNull(data.getHabits());
        assertTrue(data.getHabits().isEmpty());
    }

    @Test
    void testListConstructor() {
        List<Habit> habits = new ArrayList<>();
        habits.add(new Habit("Exercise", "Test"));
        habits.add(new Habit("Reading", "Test"));

        SyncData data = new SyncData(habits);
        assertEquals(2, data.getHabits().size());
    }

    @Test
    void testNullListConstructorCreatesEmptyList() {
        SyncData data = new SyncData(null);
        assertNotNull(data.getHabits());
        assertTrue(data.getHabits().isEmpty());
    }

    @Test
    void testSetHabits() {
        SyncData data = new SyncData();
        List<Habit> habits = new ArrayList<>();
        habits.add(new Habit("Test", "Test"));
        data.setHabits(habits);
        assertEquals(1, data.getHabits().size());
    }
}
