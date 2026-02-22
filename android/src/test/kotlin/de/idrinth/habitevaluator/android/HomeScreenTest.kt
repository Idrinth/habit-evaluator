package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.shared.model.Habit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for logic equivalent to what HomeFragment previously provided.
 * The isCategoryUsed helper is now a pure function duplicated here
 * since the logic belongs to screen-level filtering of categories.
 */
class HomeScreenTest {

    /**
     * Replication of the isCategoryUsed logic that was on HomeFragment.
     * A category is "used" if at least one habit in the list has that categoryId.
     */
    private fun isCategoryUsed(categoryId: String?, habits: List<Habit>?): Boolean {
        if (categoryId.isNullOrEmpty() || habits == null) return false
        return habits.any { it.categoryId == categoryId }
    }

    @Test
    fun testIsCategoryUsedWithMatchingCategory() {
        val habits = mutableListOf<Habit>()
        val habit = Habit("Test", "desc")
        habit.categoryId = "cat-1"
        habits.add(habit)

        assertTrue(isCategoryUsed("cat-1", habits))
    }

    @Test
    fun testIsCategoryUsedWithNoMatchingCategory() {
        val habits = mutableListOf<Habit>()
        val habit = Habit("Test", "desc")
        habit.categoryId = "cat-2"
        habits.add(habit)

        assertFalse(isCategoryUsed("cat-1", habits))
    }

    @Test
    fun testIsCategoryUsedWithEmptyList() {
        assertFalse(isCategoryUsed("cat-1", emptyList()))
    }

    @Test
    fun testIsCategoryUsedWithNullList() {
        assertFalse(isCategoryUsed("cat-1", null))
    }

    @Test
    fun testIsCategoryUsedWithNullCategoryId() {
        val habits = mutableListOf<Habit>()
        val habit = Habit("Test", "desc")
        habit.categoryId = "cat-1"
        habits.add(habit)

        assertFalse(isCategoryUsed(null, habits))
    }

    @Test
    fun testIsCategoryUsedWithEmptyCategoryId() {
        val habits = mutableListOf<Habit>()
        val habit = Habit("Test", "desc")
        habit.categoryId = "cat-1"
        habits.add(habit)

        assertFalse(isCategoryUsed("", habits))
    }

    @Test
    fun testIsCategoryUsedWithMultipleHabitsOneMatch() {
        val habits = mutableListOf<Habit>()
        val habit1 = Habit("Test1", "desc1").apply { categoryId = "cat-1" }
        val habit2 = Habit("Test2", "desc2").apply { categoryId = "cat-2" }
        val habit3 = Habit("Test3", "desc3").apply { categoryId = "cat-3" }
        habits.addAll(listOf(habit1, habit2, habit3))

        assertTrue(isCategoryUsed("cat-2", habits))
    }

    @Test
    fun testIsCategoryUsedWithMultipleHabitsSameCategory() {
        val habits = mutableListOf<Habit>()
        val habit1 = Habit("Test1", "desc1").apply { categoryId = "cat-1" }
        val habit2 = Habit("Test2", "desc2").apply { categoryId = "cat-1" }
        habits.addAll(listOf(habit1, habit2))

        assertTrue(isCategoryUsed("cat-1", habits))
    }

    @Test
    fun testIsCategoryUsedWithHabitWithNullCategoryId() {
        val habits = mutableListOf<Habit>()
        // categoryId is null by default
        habits.add(Habit("Test", "desc"))

        assertFalse(isCategoryUsed("cat-1", habits))
    }

    @Test
    fun testIsCategoryUsedNullCategoryAndNullList() {
        assertFalse(isCategoryUsed(null, null))
    }

    @Test
    fun testIsCategoryUsedEmptyCategoryAndNullList() {
        assertFalse(isCategoryUsed("", null))
    }

    @Test
    fun testIsCategoryUsedWithLargeList() {
        val habits = (0 until 100).map { i ->
            Habit("Habit $i", "desc $i").apply { categoryId = "cat-$i" }
        }
        assertTrue(isCategoryUsed("cat-50", habits))
        assertFalse(isCategoryUsed("cat-200", habits))
    }

    @Test
    fun testIsCategoryUsedWithLastItemMatching() {
        val habits = (0 until 10).map { i ->
            Habit("Habit $i", "desc $i").apply { categoryId = "cat-$i" }
        }
        assertTrue(isCategoryUsed("cat-9", habits))
    }

    @Test
    fun testIsCategoryUsedWithFirstItemMatching() {
        val habits = (0 until 10).map { i ->
            Habit("Habit $i", "desc $i").apply { categoryId = "cat-$i" }
        }
        assertTrue(isCategoryUsed("cat-0", habits))
    }

    @Test
    fun testIsCategoryUsedWithMixedNullAndSetCategories() {
        val habit1 = Habit("Test1", "desc1") // no categoryId
        val habit2 = Habit("Test2", "desc2").apply { categoryId = "cat-1" }

        assertTrue(isCategoryUsed("cat-1", listOf(habit1, habit2)))
    }
}
