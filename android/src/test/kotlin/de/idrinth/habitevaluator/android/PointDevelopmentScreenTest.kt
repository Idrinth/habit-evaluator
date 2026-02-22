package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.Habit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for habit lookup logic equivalent to what PointDevelopmentFragment provided.
 * Finds a Habit from a list by its ID, returning null if not found.
 */
class PointDevelopmentScreenTest {

    /**
     * Replication of findHabitById logic from PointDevelopmentFragment.
     */
    private fun findHabitById(habits: List<Habit>?, id: String?): Habit? {
        if (habits == null || id == null) return null
        return habits.firstOrNull { it.id == id }
    }

    @Test
    fun testFindHabitByIdWithMatchingHabit() {
        val habit = Habit("Exercise", "Daily exercise")
        val id = habit.id
        val habits = listOf(habit)

        assertSame(habit, findHabitById(habits, id))
    }

    @Test
    fun testFindHabitByIdWithNoMatch() {
        val habit = Habit("Exercise", "Daily exercise")
        val habits = listOf(habit)

        assertNull(findHabitById(habits, "non-existent-id"))
    }

    @Test
    fun testFindHabitByIdWithNullList() {
        assertNull(findHabitById(null, "some-id"))
    }

    @Test
    fun testFindHabitByIdWithNullId() {
        val habits = listOf(Habit("Exercise", "Daily exercise"))

        assertNull(findHabitById(habits, null))
    }

    @Test
    fun testFindHabitByIdWithBothNull() {
        assertNull(findHabitById(null, null))
    }

    @Test
    fun testFindHabitByIdWithEmptyList() {
        assertNull(findHabitById(emptyList(), "some-id"))
    }

    @Test
    fun testFindHabitByIdWithMultipleHabitsReturnsCorrectOne() {
        val habit1 = Habit("Exercise", "Daily exercise")
        val habit2 = Habit("Reading", "Read 30 min")
        val habit3 = Habit("Meditation", "Morning meditation")
        val habits = listOf(habit1, habit2, habit3)

        assertSame(habit2, findHabitById(habits, habit2.id))
    }

    @Test
    fun testFindHabitByIdReturnsFirstMatchWhenDuplicateIds() {
        val habit1 = Habit("Exercise", "v1")
        val habit2 = Habit("Reading", "v2")
        val habits = listOf(habit1, habit2)

        val result = findHabitById(habits, habit1.id)
        assertSame(habit1, result)
    }
}
