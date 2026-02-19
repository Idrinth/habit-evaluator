package de.idrinth.habitevaluator.android;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.idrinth.habitevaluator.android.ui.ScreenPagerAdapter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: verifies invariants that, if violated, would cause the app
 * to crash on startup or shortly after. These are static-state and constant
 * checks that can run without an Android context.
 */
class StartupRegressionTest {

    @BeforeEach
    void setUp() {
        // Reset mutable static state so tests are isolated
        MainActivity.setEditHabitId(null);
        MainActivity.setPointDevelopmentHabitId(null);
        MainActivity.setRecordEmotionPairId(null);
    }

    // ── Shared collection fields must never be null ─────────────────────────

    @Test
    void testSharedCategoriesIsNeverNull() {
        assertNotNull(
                MainActivity.getSharedCategories()
        ,
                "sharedCategories must be initialized to a non-null list to prevent NPE in fragments");
    }

    @Test
    void testSharedEmotionPairsIsNeverNull() {
        assertNotNull(
                MainActivity.getSharedEmotionPairs()
        ,
                "sharedEmotionPairs must be initialized to a non-null list to prevent NPE in fragments");
    }

    @Test
    void testSharedSleepEntriesIsNeverNull() {
        assertNotNull(
                MainActivity.getSharedSleepEntries()
        ,
                "sharedSleepEntries must be initialized to a non-null list to prevent NPE in fragments");
    }

    @Test
    void testSharedMedicationsIsNeverNull() {
        assertNotNull(
                MainActivity.getSharedMedications()
        ,
                "sharedMedications must be initialized to a non-null list to prevent NPE in fragments");
    }

    // ── Navigation IDs default to null (not empty or garbage) ───────────────

    @Test
    void testEditHabitIdDefaultsToNull() {
        assertNull(
                MainActivity.getEditHabitId()
        ,
                "editHabitId must default to null so navigation guards work correctly");
    }

    @Test
    void testPointDevelopmentHabitIdDefaultsToNull() {
        assertNull(
                MainActivity.getPointDevelopmentHabitId()
        ,
                "pointDevelopmentHabitId must default to null");
    }

    @Test
    void testRecordEmotionPairIdDefaultsToNull() {
        assertNull(
                MainActivity.getRecordEmotionPairId()
        ,
                "recordEmotionPairId must default to null");
    }

    // ── Remote storage defaults to off ──────────────────────────────────────

    @Test
    void testRemoteStorageDefaultsFalse() {
        assertFalse(
                MainActivity.isSharedUsingRemoteStorage()
        ,
                "Remote storage must default to false so the app can start without network");
    }

    // ── saveAllHabits must not throw when repository is null ────────────────

    @Test
    void testSaveAllHabitsDoesNotThrowWithNullRepository() {
        // Before onCreate runs, repositories are null. This method is called
        // from fragments and must handle that gracefully.
        MainActivity.saveAllHabits();
    }

    @Test
    void testRefreshSharedMedicationsDoesNotThrowWithNullRepository() {
        MainActivity.refreshSharedMedications();
    }

    // ── ScreenPagerAdapter page constants are valid ─────────────────────────

    @Test
    void testPageCountIsPositive() {
        assertTrue(
                ScreenPagerAdapter.PAGE_COUNT > 0
        ,
                "PAGE_COUNT must be positive");
    }

    @Test
    void testHomePageIsWithinPageCount() {
        assertTrue(
                ScreenPagerAdapter.PAGE_HOME >= 0
                        && ScreenPagerAdapter.PAGE_HOME < ScreenPagerAdapter.PAGE_COUNT
        ,
                "PAGE_HOME must be a valid index within PAGE_COUNT");
    }

    @Test
    void testAllNavigablePageIndicesAreWithinRange() {
        // Every page constant referenced by navigation must be within [0, PAGE_COUNT)
        int[] allPages = {
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

        for (int page : allPages) {
            assertTrue(
                    page >= 0 && page < ScreenPagerAdapter.PAGE_COUNT
            ,
                    "Page constant " + page + " must be >= 0 and < PAGE_COUNT ("
                            + ScreenPagerAdapter.PAGE_COUNT + ")");
        }
    }

    @Test
    void testPageCountEqualsNumberOfPageConstants() {
        // If someone adds a page constant but forgets to bump PAGE_COUNT,
        // createFragment will silently return a default HomeFragment
        // for that index — or worse, the ViewPager will crash.
        int[] allPages = {
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

        assertEquals(
                ScreenPagerAdapter.PAGE_COUNT, allPages.length
        ,
                "PAGE_COUNT must equal the total number of declared page constants");
    }

    @Test
    void testPageConstantsAreContiguousFromZero() {
        // The ViewPager2 iterates from 0..PAGE_COUNT-1, so pages must be
        // contiguous to avoid unmapped gaps that would hit the default case.
        int[] allPages = {
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

        boolean[] seen = new boolean[ScreenPagerAdapter.PAGE_COUNT];
        for (int page : allPages) {
            assertFalse( seen[page],"Duplicate page index: " + page);
            seen[page] = true;
        }
        for (int i = 0; i < seen.length; i++) {
            assertTrue( seen[i],"No page constant maps to index " + i);
        }
    }
}
