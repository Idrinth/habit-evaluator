package de.idrinth.habitevaluator.android.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Field;

/**
 * Reduces the swipe sensitivity of a ViewPager2 by increasing the touch slop
 * of its internal RecyclerView. This prevents accidental page changes from
 * small horizontal movements.
 */
public final class ViewPager2SwipeSensitivityReducer {

    private ViewPager2SwipeSensitivityReducer() {
        // Utility class
    }

    /**
     * Reduces swipe sensitivity by the given multiplier.
     * A multiplier of 2 means the user must swipe twice as far before a page change triggers.
     *
     * @param viewPager2 The ViewPager2 to modify
     * @param multiplier The sensitivity reduction factor (recommended: 2-4)
     */
    public static void reduce(ViewPager2 viewPager2, int multiplier) {
        if (viewPager2 == null || multiplier < 1) {
            return;
        }

        try {
            RecyclerView recyclerView = getRecyclerView(viewPager2);
            if (recyclerView == null) {
                return;
            }

            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            int originalTouchSlop = touchSlopField.getInt(recyclerView);
            touchSlopField.setInt(recyclerView, originalTouchSlop * multiplier);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Reflection failed; swipe sensitivity remains at default
        }
    }

    private static RecyclerView getRecyclerView(ViewPager2 viewPager2) {
        View child = viewPager2.getChildAt(0);
        if (child instanceof RecyclerView) {
            return (RecyclerView) child;
        }
        return null;
    }
}
