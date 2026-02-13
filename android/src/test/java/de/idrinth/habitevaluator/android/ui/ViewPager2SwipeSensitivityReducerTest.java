package de.idrinth.habitevaluator.android.ui;

import org.junit.Test;

import static org.junit.Assert.*;

public class ViewPager2SwipeSensitivityReducerTest {

    @Test
    public void testReduceWithNullViewPagerDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, 2);
    }

    @Test
    public void testReduceWithZeroMultiplierDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, 0);
    }

    @Test
    public void testReduceWithNegativeMultiplierDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, -1);
    }

    @Test
    public void testReduceWithNullAndValidMultiplierDoesNotThrow() {
        ViewPager2SwipeSensitivityReducer.reduce(null, 4);
    }
}
