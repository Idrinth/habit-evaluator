package de.idrinth.habitevaluator.android.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.idrinth.habitevaluator.android.AddHabitFragment;
import de.idrinth.habitevaluator.android.DiaryFragment;
import de.idrinth.habitevaluator.android.EditHabitsFragment;
import de.idrinth.habitevaluator.android.EmotionalStateFragment;
import de.idrinth.habitevaluator.android.HomeFragment;
import de.idrinth.habitevaluator.android.PointDevelopmentFragment;
import de.idrinth.habitevaluator.android.SettingsFragment;
import de.idrinth.habitevaluator.android.SleepTrackingFragment;
import de.idrinth.habitevaluator.android.StatsFragment;

public class ScreenPagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_EDIT_HABITS = 0;
    public static final int PAGE_HOME = 1;
    public static final int PAGE_DIARY = 2;
    public static final int PAGE_SLEEP = 3;
    public static final int PAGE_STATS = 4;
    public static final int PAGE_EMOTIONAL_STATE = 5;
    public static final int PAGE_ADD_HABIT = 6;
    public static final int PAGE_POINT_DEVELOPMENT = 7;
    public static final int PAGE_SETTINGS = 8;
    public static final int PAGE_COUNT = 9;

    public ScreenPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case PAGE_EDIT_HABITS:
                return new EditHabitsFragment();
            case PAGE_HOME:
                return new HomeFragment();
            case PAGE_DIARY:
                return new DiaryFragment();
            case PAGE_SLEEP:
                return new SleepTrackingFragment();
            case PAGE_STATS:
                return new StatsFragment();
            case PAGE_EMOTIONAL_STATE:
                return new EmotionalStateFragment();
            case PAGE_ADD_HABIT:
                return new AddHabitFragment();
            case PAGE_POINT_DEVELOPMENT:
                return new PointDevelopmentFragment();
            case PAGE_SETTINGS:
                return new SettingsFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
