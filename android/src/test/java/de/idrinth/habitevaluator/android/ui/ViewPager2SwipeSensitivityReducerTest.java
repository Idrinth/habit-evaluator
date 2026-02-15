package de.idrinth.habitevaluator.android.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViewPager2SwipeSensitivityReducerTest {

    @Test
    void testReduceWithNullViewPagerDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, 2);
    }

    @Test
    void testReduceWithZeroMultiplierDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, 0);
    }

    @Test
    void testReduceWithNegativeMultiplierDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, -1);
    }

    @Test
    void testReduceWithNullAndValidMultiplierDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, 4);
    }

    @Test
    void testReduceWithViewPagerWithNoChildDoesNotThrow() {
        ViewPager2 viewPager2 = mock(ViewPager2.class);
        when(viewPager2.getChildAt(0)).thenReturn(null);

        ViewPager2SwipeSensitivityReducer.reduce(viewPager2, 2);
    }

    @Test
    void testReduceWithViewPagerWithNonRecyclerViewChildDoesNotThrow() {
        ViewPager2 viewPager2 = mock(ViewPager2.class);
        View nonRecyclerView = mock(View.class);
        when(viewPager2.getChildAt(0)).thenReturn(nonRecyclerView);

        ViewPager2SwipeSensitivityReducer.reduce(viewPager2, 2);
    }

    @Test
    void testReduceWithViewPagerWithRecyclerViewChild() {
        ViewPager2 viewPager2 = mock(ViewPager2.class);
        RecyclerView recyclerView = mock(RecyclerView.class);
        when(viewPager2.getChildAt(0)).thenReturn(recyclerView);

        // The reflection on mTouchSlop will fail on a mock, but should
        // be caught by the try-catch in the source and not throw
        ViewPager2SwipeSensitivityReducer.reduce(viewPager2, 2);
    }

    @Test
    void testReduceWithMultiplierOfOneDoesNotThrow() {
        ViewPager2 viewPager2 = mock(ViewPager2.class);
        RecyclerView recyclerView = mock(RecyclerView.class);
        when(viewPager2.getChildAt(0)).thenReturn(recyclerView);

        ViewPager2SwipeSensitivityReducer.reduce(viewPager2, 1);
    }

    @Test
    void testReduceWithLargeMultiplierDoesNotThrow() {
        ViewPager2 viewPager2 = mock(ViewPager2.class);
        RecyclerView recyclerView = mock(RecyclerView.class);
        when(viewPager2.getChildAt(0)).thenReturn(recyclerView);

        ViewPager2SwipeSensitivityReducer.reduce(viewPager2, 100);
    }

    @Test
    void testReduceWithMultiplierBoundaryDoesNotThrow() {
        // Multiplier of exactly 1 should still work (no-op multiplication)
        ViewPager2SwipeSensitivityReducer.reduce(null, 1);
    }
}
