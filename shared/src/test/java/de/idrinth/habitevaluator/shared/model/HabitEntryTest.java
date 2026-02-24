package de.idrinth.habitevaluator.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HabitEntryTest {

    @Test
    void testDefaultConstructor() {
        HabitEntry entry = new HabitEntry();
        assertNotNull(entry.getId());
        assertNotNull(entry.getCompletedAt());
        assertEquals(1, entry.getValue());
    }

    @Test
    void testHabitIdConstructor() {
        HabitEntry entry = new HabitEntry("habit-123");
        assertNotNull(entry.getId());
        assertEquals("habit-123", entry.getHabitId());
    }

    @Test
    void testHabitIdNotesConstructor() {
        HabitEntry entry = new HabitEntry("habit-123", "Felt great");
        assertEquals("habit-123", entry.getHabitId());
        assertEquals("Felt great", entry.getNotes());
    }

    @Test
    void testGetHabitIdWhenHabitIsNull() {
        HabitEntry entry = new HabitEntry();
        assertNull(entry.getHabitId());
    }

    @Test
    void testSetHabitIdWhenHabitIsNull() {
        HabitEntry entry = new HabitEntry();
        entry.setHabitId("new-id");
        assertEquals("new-id", entry.getHabitId());
    }

    @Test
    void testSetHabitIdWhenHabitExists() {
        HabitEntry entry = new HabitEntry("old-id");
        entry.setHabitId("new-id");
        assertEquals("new-id", entry.getHabitId());
    }

    @Test
    void testSetCompletedAt() {
        HabitEntry entry = new HabitEntry();
        LocalDateTime time = LocalDateTime.of(2024, 6, 15, 10, 30);
        entry.setCompletedAt(time);
        assertEquals(time, entry.getCompletedAt());
    }

    @Test
    void testSetValue() {
        HabitEntry entry = new HabitEntry();
        entry.setValue(5);
        assertEquals(5, entry.getValue());
    }

    @Test
    void testSetHabit() {
        HabitEntry entry = new HabitEntry();
        Habit habit = new Habit("Test", "Test");
        entry.setHabit(habit);
        assertSame(habit, entry.getHabit());
    }

    @Test
    void testEqualsSameId() {
        HabitEntry e1 = new HabitEntry();
        HabitEntry e2 = new HabitEntry();
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void testEqualsDifferentId() {
        HabitEntry e1 = new HabitEntry();
        HabitEntry e2 = new HabitEntry();
        assertNotEquals(e1, e2);
    }

    @Test
    void testEqualsNull() {
        HabitEntry e = new HabitEntry();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        HabitEntry e = new HabitEntry();
        assertEquals(e, e);
    }

    @Test
    void testHashCodeConsistent() {
        HabitEntry e1 = new HabitEntry();
        HabitEntry e2 = new HabitEntry();
        e2.setId(e1.getId());
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testHabitFieldHasJsonIgnoreToPreventCircularSerialization() throws NoSuchFieldException {
        Field habitField = HabitEntry.class.getDeclaredField("habit");
        assertNotNull(
            habitField.getAnnotation(JsonIgnore.class),
            "HabitEntry.habit must have @JsonIgnore to prevent infinite recursion during JSON serialization"
        );
    }
}
