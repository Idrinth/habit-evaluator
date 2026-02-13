package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}
