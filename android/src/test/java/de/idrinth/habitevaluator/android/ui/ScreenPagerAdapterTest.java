package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenPagerAdapterTest {

    @Test
    void testPageConstants() {
        assertEquals(0, ScreenPagerAdapter.PAGE_HOME);
        assertEquals(1, ScreenPagerAdapter.PAGE_DIARY);
        assertEquals(2, ScreenPagerAdapter.PAGE_SLEEP);
        assertEquals(3, ScreenPagerAdapter.PAGE_EMERGENCY_PLAN);
        assertEquals(4, ScreenPagerAdapter.PAGE_EMOTIONAL_STATE);
        assertEquals(5, ScreenPagerAdapter.PAGE_EDIT_HABITS);
        assertEquals(6, ScreenPagerAdapter.PAGE_ADD_HABIT);
        assertEquals(7, ScreenPagerAdapter.PAGE_STATS);
        assertEquals(8, ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT);
        assertEquals(9, ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR);
        assertEquals(10, ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY);
        assertEquals(11, ScreenPagerAdapter.PAGE_SETTINGS);
        assertEquals(12, ScreenPagerAdapter.PAGE_IMPRINT);
        assertEquals(13, ScreenPagerAdapter.PAGE_POSITIVITY_DIARY);
        assertEquals(14, ScreenPagerAdapter.PAGE_SPORT_LOG);
        assertEquals(15, ScreenPagerAdapter.PAGE_FOOD_LOG);
        assertEquals(16, ScreenPagerAdapter.PAGE_MEDICATION_LOG);
        assertEquals(17, ScreenPagerAdapter.PAGE_MEDICATION_LIST);
        assertEquals(18, ScreenPagerAdapter.PAGE_ACTIVITY_LOG);
    }

    @Test
    void testPageCount() {
        assertEquals(19, ScreenPagerAdapter.PAGE_COUNT);
    }

    @Test
    void testPageCountMatchesLastPagePlusOne() {
        assertEquals(ScreenPagerAdapter.PAGE_ACTIVITY_LOG + 1, ScreenPagerAdapter.PAGE_COUNT);
    }

    @Test
    void testAllPageIndicesAreUnique() {
        int[] pages = {
            ScreenPagerAdapter.PAGE_HOME,
            ScreenPagerAdapter.PAGE_DIARY,
            ScreenPagerAdapter.PAGE_SLEEP,
            ScreenPagerAdapter.PAGE_EMERGENCY_PLAN,
            ScreenPagerAdapter.PAGE_EMOTIONAL_STATE,
            ScreenPagerAdapter.PAGE_EDIT_HABITS,
            ScreenPagerAdapter.PAGE_ADD_HABIT,
            ScreenPagerAdapter.PAGE_STATS,
            ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT,
            ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR,
            ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY,
            ScreenPagerAdapter.PAGE_SETTINGS,
            ScreenPagerAdapter.PAGE_IMPRINT,
            ScreenPagerAdapter.PAGE_POSITIVITY_DIARY,
            ScreenPagerAdapter.PAGE_SPORT_LOG,
            ScreenPagerAdapter.PAGE_FOOD_LOG,
            ScreenPagerAdapter.PAGE_MEDICATION_LOG,
            ScreenPagerAdapter.PAGE_MEDICATION_LIST,
            ScreenPagerAdapter.PAGE_ACTIVITY_LOG
        };

        for (int i = 0; i < pages.length; i++) {
            for (int j = i + 1; j < pages.length; j++) {
                assertNotEquals(
                    pages[i], pages[j],"Page indices " + i + " and " + j + " should be unique");
            }
        }
    }

    @Test
    void testPageIndicesAreContiguous() {
        int[] pages = {
            ScreenPagerAdapter.PAGE_HOME,
            ScreenPagerAdapter.PAGE_DIARY,
            ScreenPagerAdapter.PAGE_SLEEP,
            ScreenPagerAdapter.PAGE_EMERGENCY_PLAN,
            ScreenPagerAdapter.PAGE_EMOTIONAL_STATE,
            ScreenPagerAdapter.PAGE_EDIT_HABITS,
            ScreenPagerAdapter.PAGE_ADD_HABIT,
            ScreenPagerAdapter.PAGE_STATS,
            ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT,
            ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR,
            ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY,
            ScreenPagerAdapter.PAGE_SETTINGS,
            ScreenPagerAdapter.PAGE_IMPRINT,
            ScreenPagerAdapter.PAGE_POSITIVITY_DIARY,
            ScreenPagerAdapter.PAGE_SPORT_LOG,
            ScreenPagerAdapter.PAGE_FOOD_LOG,
            ScreenPagerAdapter.PAGE_MEDICATION_LOG,
            ScreenPagerAdapter.PAGE_MEDICATION_LIST,
            ScreenPagerAdapter.PAGE_ACTIVITY_LOG
        };

        assertEquals(ScreenPagerAdapter.PAGE_COUNT, pages.length);
        java.util.Arrays.sort(pages);
        for (int i = 0; i < pages.length; i++) {
            assertEquals(i, pages[i]);
        }
    }

    @Test
    void testLastSwipeablePageIsEmotionalState() {
        assertEquals(ScreenPagerAdapter.PAGE_EMOTIONAL_STATE, ScreenPagerAdapter.LAST_SWIPEABLE_PAGE);
    }

    @Test
    void testBottomNavPagesAreFirstFivePositions() {
        assertEquals(0, ScreenPagerAdapter.PAGE_HOME);
        assertEquals(1, ScreenPagerAdapter.PAGE_DIARY);
        assertEquals(2, ScreenPagerAdapter.PAGE_SLEEP);
        assertEquals(3, ScreenPagerAdapter.PAGE_EMERGENCY_PLAN);
        assertEquals(4, ScreenPagerAdapter.PAGE_EMOTIONAL_STATE);
    }

    @Test
    void testProgrammaticOnlyPagesAreAfterSwipeablePages() {
        int[] programmaticPages = {
            ScreenPagerAdapter.PAGE_EDIT_HABITS,
            ScreenPagerAdapter.PAGE_ADD_HABIT,
            ScreenPagerAdapter.PAGE_STATS,
            ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT,
            ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR,
            ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY,
            ScreenPagerAdapter.PAGE_SETTINGS,
            ScreenPagerAdapter.PAGE_IMPRINT,
            ScreenPagerAdapter.PAGE_POSITIVITY_DIARY,
            ScreenPagerAdapter.PAGE_SPORT_LOG,
            ScreenPagerAdapter.PAGE_FOOD_LOG,
            ScreenPagerAdapter.PAGE_MEDICATION_LOG,
            ScreenPagerAdapter.PAGE_MEDICATION_LIST,
            ScreenPagerAdapter.PAGE_ACTIVITY_LOG
        };

        for (int page : programmaticPages) {
            assertTrue(
                page > ScreenPagerAdapter.LAST_SWIPEABLE_PAGE,
                "Programmatic page " + page + " must be after LAST_SWIPEABLE_PAGE ("
                    + ScreenPagerAdapter.LAST_SWIPEABLE_PAGE + ")");
        }
    }
}
