package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HabitTest {

    @Test
    void testDefaultConstructorSetsDefaults() {
        Habit habit = new Habit();
        assertNotNull(habit.getId());
        assertNotNull(habit.getCreatedAt());
        assertNotNull(habit.getEntries());
        assertTrue(habit.getEntries().isEmpty());
        assertEquals(FrequencyType.DAILY, habit.getFrequencyType());
        assertEquals(1, habit.getTargetFrequency());
        assertEquals(1, habit.getMaxEntriesPerDay());
        assertTrue(habit.isPositiveScoring());
        assertNotNull(habit.getScoringRule());
    }

    @Test
    void testNameDescriptionConstructor() {
        Habit habit = new Habit("Exercise", "Daily workout");
        assertEquals("Exercise", habit.getName());
        assertEquals("Daily workout", habit.getDescription());
        assertNotNull(habit.getId());
    }

    @Test
    void testAddEntrySetsHabitReference() {
        Habit habit = new Habit("Test", "Test");
        HabitEntry entry = new HabitEntry();

        habit.addEntry(entry);

        assertEquals(1, habit.getEntries().size());
        assertSame(habit, entry.getHabit());
    }

    @Test
    void testSetFrequencyType() {
        Habit habit = new Habit();
        habit.setFrequencyType(FrequencyType.WEEKLY);
        assertEquals(FrequencyType.WEEKLY, habit.getFrequencyType());
    }

    @Test
    void testSetTargetFrequency() {
        Habit habit = new Habit();
        habit.setTargetFrequency(3);
        assertEquals(3, habit.getTargetFrequency());
    }

    @Test
    void testSetCategoryId() {
        Habit habit = new Habit();
        habit.setCategoryId("cat-1");
        assertEquals("cat-1", habit.getCategoryId());
    }

    @Test
    void testSetUser() {
        Habit habit = new Habit();
        User user = new User("test", "pass");
        habit.setUser(user);
        assertSame(user, habit.getUser());
    }

    @Test
    void testEqualsSameId() {
        Habit h1 = new Habit("A", "A");
        Habit h2 = new Habit("B", "B");
        h2.setId(h1.getId());
        assertEquals(h1, h2);
    }

    @Test
    void testEqualsDifferentId() {
        Habit h1 = new Habit("A", "A");
        Habit h2 = new Habit("A", "A");
        assertNotEquals(h1, h2);
    }

    @Test
    void testEqualsNull() {
        Habit h = new Habit();
        assertNotEquals(null, h);
    }

    @Test
    void testEqualsSameObject() {
        Habit h = new Habit();
        assertEquals(h, h);
    }

    @Test
    void testSetPositiveScoring() {
        Habit habit = new Habit();
        assertTrue(habit.isPositiveScoring());
        habit.setPositiveScoring(false);
        assertFalse(habit.isPositiveScoring());
    }

    @Test
    void testHashCodeConsistent() {
        Habit h1 = new Habit();
        Habit h2 = new Habit();
        h2.setId(h1.getId());
        assertEquals(h1.hashCode(), h2.hashCode());
    }

    @Test
    void testSetMaxEntriesPerDay() {
        Habit habit = new Habit();
        habit.setMaxEntriesPerDay(3);
        assertEquals(3, habit.getMaxEntriesPerDay());
    }

    @Test
    void testHasReachedDailyLimitWithNoEntries() {
        Habit habit = new Habit();
        habit.setMaxEntriesPerDay(1);
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()));
    }

    @Test
    void testHasReachedDailyLimitWithEntriesAtLimit() {
        Habit habit = new Habit("Test", "Test");
        habit.setMaxEntriesPerDay(1);
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.now());
        habit.addEntry(entry);
        assertTrue(habit.hasReachedDailyLimit(LocalDate.now()));
    }

    @Test
    void testHasReachedDailyLimitWithEntriesBelowLimit() {
        Habit habit = new Habit("Test", "Test");
        habit.setMaxEntriesPerDay(2);
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.now());
        habit.addEntry(entry);
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()));
    }

    @Test
    void testHasReachedDailyLimitUnlimited() {
        Habit habit = new Habit("Test", "Test");
        habit.setMaxEntriesPerDay(0);
        for (int i = 0; i < 10; i++) {
            HabitEntry entry = new HabitEntry();
            entry.setCompletedAt(LocalDateTime.now());
            habit.addEntry(entry);
        }
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()));
    }

    @Test
    void testHasReachedDailyLimitOnlyCountsToday() {
        Habit habit = new Habit("Test", "Test");
        habit.setMaxEntriesPerDay(1);
        HabitEntry yesterdayEntry = new HabitEntry();
        yesterdayEntry.setCompletedAt(LocalDateTime.now().minusDays(1));
        habit.addEntry(yesterdayEntry);
        assertFalse(habit.hasReachedDailyLimit(LocalDate.now()));
    }
}
