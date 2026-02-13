package de.idrinth.habitevaluator.android;

import org.junit.Before;
import org.junit.Test;

import de.idrinth.habitevaluator.android.ui.ScreenPagerAdapter;

import static org.junit.Assert.*;

/**
 * Regression test: verifies invariants that, if violated, would cause the app
 * to crash on startup or shortly after. These are static-state and constant
 * checks that can run without an Android context.
 */
public class StartupRegressionTest {

    @Before
    public void setUp() {
        // Reset mutable static state so tests are isolated
        MainActivity.setEditHabitId(null);
        MainActivity.setPointDevelopmentHabitId(null);
        MainActivity.setRecordEmotionPairId(null);
    }

    // ── Shared collection fields must never be null ─────────────────────────

    @Test
    public void testSharedCategoriesIsNeverNull() {
        assertNotNull(
                "sharedCategories must be initialized to a non-null list to prevent NPE in fragments",
                MainActivity.getSharedCategories()
        );
    }

    @Test
    public void testSharedEmotionPairsIsNeverNull() {
        assertNotNull(
                "sharedEmotionPairs must be initialized to a non-null list to prevent NPE in fragments",
                MainActivity.getSharedEmotionPairs()
        );
    }

    @Test
    public void testSharedSleepEntriesIsNeverNull() {
        assertNotNull(
                "sharedSleepEntries must be initialized to a non-null list to prevent NPE in fragments",
                MainActivity.getSharedSleepEntries()
        );
    }

    @Test
    public void testSharedMedicationsIsNeverNull() {
        assertNotNull(
                "sharedMedications must be initialized to a non-null list to prevent NPE in fragments",
                MainActivity.getSharedMedications()
        );
    }

    // ── Navigation IDs default to null (not empty or garbage) ───────────────

    @Test
    public void testEditHabitIdDefaultsToNull() {
        assertNull(
                "editHabitId must default to null so navigation guards work correctly",
                MainActivity.getEditHabitId()
        );
    }

    @Test
    public void testPointDevelopmentHabitIdDefaultsToNull() {
        assertNull(
                "pointDevelopmentHabitId must default to null",
                MainActivity.getPointDevelopmentHabitId()
        );
    }

    @Test
    public void testRecordEmotionPairIdDefaultsToNull() {
        assertNull(
                "recordEmotionPairId must default to null",
                MainActivity.getRecordEmotionPairId()
        );
    }

    // ── Remote storage defaults to off ──────────────────────────────────────

    @Test
    public void testRemoteStorageDefaultsFalse() {
        assertFalse(
                "Remote storage must default to false so the app can start without network",
                MainActivity.isSharedUsingRemoteStorage()
        );
    }

    // ── saveAllHabits must not throw when repository is null ────────────────

    @Test
    public void testSaveAllHabitsDoesNotThrowWithNullRepository() {
        // Before onCreate runs, repositories are null. This method is called
        // from fragments and must handle that gracefully.
        MainActivity.saveAllHabits();
    }

    @Test
    public void testRefreshSharedMedicationsDoesNotThrowWithNullRepository() {
        MainActivity.refreshSharedMedications();
    }

    // ── ScreenPagerAdapter page constants are valid ─────────────────────────

    @Test
    public void testPageCountIsPositive() {
        assertTrue(
                "PAGE_COUNT must be positive",
                ScreenPagerAdapter.PAGE_COUNT > 0
        );
    }

    @Test
    public void testHomePageIsWithinPageCount() {
        assertTrue(
                "PAGE_HOME must be a valid index within PAGE_COUNT",
                ScreenPagerAdapter.PAGE_HOME >= 0
                        && ScreenPagerAdapter.PAGE_HOME < ScreenPagerAdapter.PAGE_COUNT
        );
    }

    @Test
    public void testAllNavigablePageIndicesAreWithinRange() {
        // Every page constant referenced by navigation must be within [0, PAGE_COUNT)
        int[] allPages = {
                ScreenPagerAdapter.PAGE_EDIT_HABITS,
                ScreenPagerAdapter.PAGE_HOME,
                ScreenPagerAdapter.PAGE_DIARY,
                ScreenPagerAdapter.PAGE_SLEEP,
                ScreenPagerAdapter.PAGE_STATS,
                ScreenPagerAdapter.PAGE_EMOTIONAL_STATE,
                ScreenPagerAdapter.PAGE_ADD_HABIT,
                ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT,
                ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR,
                ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY,
                ScreenPagerAdapter.PAGE_SETTINGS,
                ScreenPagerAdapter.PAGE_IMPRINT,
                ScreenPagerAdapter.PAGE_POSITIVITY_DIARY,
                ScreenPagerAdapter.PAGE_SPORT_LOG,
                ScreenPagerAdapter.PAGE_FOOD_LOG,
                ScreenPagerAdapter.PAGE_MEDICATION_LOG,
                ScreenPagerAdapter.PAGE_MEDICATION_LIST
        };

        for (int page : allPages) {
            assertTrue(
                    "Page constant " + page + " must be >= 0 and < PAGE_COUNT ("
                            + ScreenPagerAdapter.PAGE_COUNT + ")",
                    page >= 0 && page < ScreenPagerAdapter.PAGE_COUNT
            );
        }
    }

    @Test
    public void testPageCountEqualsNumberOfPageConstants() {
        // If someone adds a page constant but forgets to bump PAGE_COUNT,
        // createFragment will silently return a default HomeFragment
        // for that index — or worse, the ViewPager will crash.
        int[] allPages = {
                ScreenPagerAdapter.PAGE_EDIT_HABITS,
                ScreenPagerAdapter.PAGE_HOME,
                ScreenPagerAdapter.PAGE_DIARY,
                ScreenPagerAdapter.PAGE_SLEEP,
                ScreenPagerAdapter.PAGE_STATS,
                ScreenPagerAdapter.PAGE_EMOTIONAL_STATE,
                ScreenPagerAdapter.PAGE_ADD_HABIT,
                ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT,
                ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR,
                ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY,
                ScreenPagerAdapter.PAGE_SETTINGS,
                ScreenPagerAdapter.PAGE_IMPRINT,
                ScreenPagerAdapter.PAGE_POSITIVITY_DIARY,
                ScreenPagerAdapter.PAGE_SPORT_LOG,
                ScreenPagerAdapter.PAGE_FOOD_LOG,
                ScreenPagerAdapter.PAGE_MEDICATION_LOG,
                ScreenPagerAdapter.PAGE_MEDICATION_LIST
        };

        assertEquals(
                "PAGE_COUNT must equal the total number of declared page constants",
                ScreenPagerAdapter.PAGE_COUNT, allPages.length
        );
    }

    @Test
    public void testPageConstantsAreContiguousFromZero() {
        // The ViewPager2 iterates from 0..PAGE_COUNT-1, so pages must be
        // contiguous to avoid unmapped gaps that would hit the default case.
        int[] allPages = {
                ScreenPagerAdapter.PAGE_EDIT_HABITS,
                ScreenPagerAdapter.PAGE_HOME,
                ScreenPagerAdapter.PAGE_DIARY,
                ScreenPagerAdapter.PAGE_SLEEP,
                ScreenPagerAdapter.PAGE_STATS,
                ScreenPagerAdapter.PAGE_EMOTIONAL_STATE,
                ScreenPagerAdapter.PAGE_ADD_HABIT,
                ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT,
                ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR,
                ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY,
                ScreenPagerAdapter.PAGE_SETTINGS,
                ScreenPagerAdapter.PAGE_IMPRINT,
                ScreenPagerAdapter.PAGE_POSITIVITY_DIARY,
                ScreenPagerAdapter.PAGE_SPORT_LOG,
                ScreenPagerAdapter.PAGE_FOOD_LOG,
                ScreenPagerAdapter.PAGE_MEDICATION_LOG,
                ScreenPagerAdapter.PAGE_MEDICATION_LIST
        };

        boolean[] seen = new boolean[ScreenPagerAdapter.PAGE_COUNT];
        for (int page : allPages) {
            assertFalse("Duplicate page index: " + page, seen[page]);
            seen[page] = true;
        }
        for (int i = 0; i < seen.length; i++) {
            assertTrue("No page constant maps to index " + i, seen[i]);
        }
    }
}
