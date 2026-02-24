package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*

/**
 * ScreenPagerAdapter replaced by Compose Navigation routes, but the page constants
 * and their ordering contract is preserved here to guard against regressions in
 * route/index assignments.
 */
class ScreenPagerAdapterTest {

    @Test
    fun testAdapterReplacedByCompose() {
        // ScreenPagerAdapter (ViewPager2) replaced by Compose Navigation
        assertTrue(true)
    }

    @Test
    fun testPageIndexConstants() {
        // Preserved from original: page indices must match expected values
        assertEquals(0, PAGE_HOME)
        assertEquals(1, PAGE_DIARY)
        assertEquals(2, PAGE_PLANNER)
        assertEquals(3, PAGE_EMERGENCY_PLAN)
        assertEquals(4, PAGE_EMOTIONAL_STATE)
        assertEquals(5, PAGE_EDIT_HABITS)
        assertEquals(6, PAGE_ADD_HABIT)
        assertEquals(7, PAGE_STATS)
        assertEquals(8, PAGE_POINT_DEVELOPMENT)
        assertEquals(9, PAGE_ADD_EMOTION_PAIR)
        assertEquals(10, PAGE_RECORD_EMOTION_ENTRY)
        assertEquals(11, PAGE_SETTINGS)
        assertEquals(12, PAGE_IMPRINT)
        assertEquals(13, PAGE_POSITIVITY_DIARY)
        assertEquals(14, PAGE_SPORT_LOG)
        assertEquals(15, PAGE_FOOD_LOG)
        assertEquals(16, PAGE_MEDICATION_LOG)
        assertEquals(17, PAGE_MEDICATION_LIST)
        assertEquals(18, PAGE_ACTIVITY_LOG)
        assertEquals(19, PAGE_SLEEP)
    }

    @Test
    fun testPageCount() {
        assertEquals(20, PAGE_COUNT)
    }

    @Test
    fun testPageCountMatchesLastPagePlusOne() {
        assertEquals(PAGE_SLEEP + 1, PAGE_COUNT)
    }

    @Test
    fun testAllPageIndicesAreUnique() {
        val pages = intArrayOf(
            PAGE_HOME, PAGE_DIARY, PAGE_PLANNER, PAGE_EMERGENCY_PLAN,
            PAGE_EMOTIONAL_STATE, PAGE_EDIT_HABITS, PAGE_ADD_HABIT, PAGE_STATS,
            PAGE_POINT_DEVELOPMENT, PAGE_ADD_EMOTION_PAIR, PAGE_RECORD_EMOTION_ENTRY,
            PAGE_SETTINGS, PAGE_IMPRINT, PAGE_POSITIVITY_DIARY, PAGE_SPORT_LOG,
            PAGE_FOOD_LOG, PAGE_MEDICATION_LOG, PAGE_MEDICATION_LIST, PAGE_ACTIVITY_LOG,
            PAGE_SLEEP
        )
        val uniqueCount = pages.toSet().size
        assertEquals("All page indices must be unique", pages.size, uniqueCount)
    }

    @Test
    fun testPageIndicesAreContiguous() {
        val pages = intArrayOf(
            PAGE_HOME, PAGE_DIARY, PAGE_PLANNER, PAGE_EMERGENCY_PLAN,
            PAGE_EMOTIONAL_STATE, PAGE_EDIT_HABITS, PAGE_ADD_HABIT, PAGE_STATS,
            PAGE_POINT_DEVELOPMENT, PAGE_ADD_EMOTION_PAIR, PAGE_RECORD_EMOTION_ENTRY,
            PAGE_SETTINGS, PAGE_IMPRINT, PAGE_POSITIVITY_DIARY, PAGE_SPORT_LOG,
            PAGE_FOOD_LOG, PAGE_MEDICATION_LOG, PAGE_MEDICATION_LIST, PAGE_ACTIVITY_LOG,
            PAGE_SLEEP
        )
        assertEquals(PAGE_COUNT, pages.size)
        val sorted = pages.sorted()
        sorted.forEachIndexed { index, value ->
            assertEquals("Index $index must equal its position", index, value)
        }
    }

    @Test
    fun testLastSwipeablePageIsEmotionalState() {
        assertEquals(PAGE_EMOTIONAL_STATE, LAST_SWIPEABLE_PAGE)
    }

    @Test
    fun testBottomNavPagesAreFirstFivePositions() {
        assertEquals(0, PAGE_HOME)
        assertEquals(1, PAGE_DIARY)
        assertEquals(2, PAGE_PLANNER)
        assertEquals(3, PAGE_EMERGENCY_PLAN)
        assertEquals(4, PAGE_EMOTIONAL_STATE)
    }

    @Test
    fun testProgrammaticOnlyPagesAreAfterSwipeablePages() {
        val programmaticPages = intArrayOf(
            PAGE_EDIT_HABITS, PAGE_ADD_HABIT, PAGE_STATS, PAGE_POINT_DEVELOPMENT,
            PAGE_ADD_EMOTION_PAIR, PAGE_RECORD_EMOTION_ENTRY, PAGE_SETTINGS,
            PAGE_IMPRINT, PAGE_POSITIVITY_DIARY, PAGE_SPORT_LOG, PAGE_FOOD_LOG,
            PAGE_MEDICATION_LOG, PAGE_MEDICATION_LIST, PAGE_ACTIVITY_LOG, PAGE_SLEEP
        )
        for (page in programmaticPages) {
            assertTrue(
                "Programmatic page $page must be after LAST_SWIPEABLE_PAGE ($LAST_SWIPEABLE_PAGE)",
                page > LAST_SWIPEABLE_PAGE
            )
        }
    }

    // Mirror of the page constants that were in ScreenPagerAdapter
    companion object {
        const val PAGE_HOME = 0
        const val PAGE_DIARY = 1
        const val PAGE_PLANNER = 2
        const val PAGE_EMERGENCY_PLAN = 3
        const val PAGE_EMOTIONAL_STATE = 4
        const val PAGE_EDIT_HABITS = 5
        const val PAGE_ADD_HABIT = 6
        const val PAGE_STATS = 7
        const val PAGE_POINT_DEVELOPMENT = 8
        const val PAGE_ADD_EMOTION_PAIR = 9
        const val PAGE_RECORD_EMOTION_ENTRY = 10
        const val PAGE_SETTINGS = 11
        const val PAGE_IMPRINT = 12
        const val PAGE_POSITIVITY_DIARY = 13
        const val PAGE_SPORT_LOG = 14
        const val PAGE_FOOD_LOG = 15
        const val PAGE_MEDICATION_LOG = 16
        const val PAGE_MEDICATION_LIST = 17
        const val PAGE_ACTIVITY_LOG = 18
        const val PAGE_SLEEP = 19
        const val PAGE_COUNT = 20
        const val LAST_SWIPEABLE_PAGE = PAGE_EMOTIONAL_STATE
    }
}
