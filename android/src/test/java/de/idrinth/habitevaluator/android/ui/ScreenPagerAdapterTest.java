package de.idrinth.habitevaluator.android.ui;

import org.junit.Test;

import static org.junit.Assert.*;

public class ScreenPagerAdapterTest {

    @Test
    public void testPageConstants() {
        assertEquals(0, ScreenPagerAdapter.PAGE_EDIT_HABITS);
        assertEquals(1, ScreenPagerAdapter.PAGE_HOME);
        assertEquals(2, ScreenPagerAdapter.PAGE_DIARY);
        assertEquals(3, ScreenPagerAdapter.PAGE_SLEEP);
        assertEquals(4, ScreenPagerAdapter.PAGE_STATS);
        assertEquals(5, ScreenPagerAdapter.PAGE_EMOTIONAL_STATE);
        assertEquals(6, ScreenPagerAdapter.PAGE_ADD_HABIT);
        assertEquals(7, ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT);
        assertEquals(8, ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR);
        assertEquals(9, ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY);
        assertEquals(10, ScreenPagerAdapter.PAGE_SETTINGS);
        assertEquals(11, ScreenPagerAdapter.PAGE_IMPRINT);
    }

    @Test
    public void testPageCount() {
        assertEquals(12, ScreenPagerAdapter.PAGE_COUNT);
    }

    @Test
    public void testPageCountMatchesLastPagePlusOne() {
        assertEquals(ScreenPagerAdapter.PAGE_IMPRINT + 1, ScreenPagerAdapter.PAGE_COUNT);
    }

    @Test
    public void testAllPageIndicesAreUnique() {
        int[] pages = {
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
            ScreenPagerAdapter.PAGE_IMPRINT
        };

        for (int i = 0; i < pages.length; i++) {
            for (int j = i + 1; j < pages.length; j++) {
                assertNotEquals("Page indices " + i + " and " + j + " should be unique",
                    pages[i], pages[j]);
            }
        }
    }

    @Test
    public void testPageIndicesAreContiguous() {
        int[] pages = {
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
            ScreenPagerAdapter.PAGE_IMPRINT
        };

        assertEquals(ScreenPagerAdapter.PAGE_COUNT, pages.length);
        for (int i = 0; i < pages.length; i++) {
            assertEquals(i, pages[i]);
        }
    }
}
