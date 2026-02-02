package de.idrinth.habitevaluator.android.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.idrinth.habitevaluator.android.AddHabitFragment;
import de.idrinth.habitevaluator.android.EditHabitsFragment;
import de.idrinth.habitevaluator.android.HomeFragment;
import de.idrinth.habitevaluator.android.SettingsFragment;

public class ScreenPagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_SETTINGS = 0;
    public static final int PAGE_HOME = 1;
    public static final int PAGE_ADD_HABIT = 2;
    public static final int PAGE_EDIT_HABITS = 3;
    public static final int PAGE_COUNT = 4;

    public ScreenPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case PAGE_SETTINGS:
                return new SettingsFragment();
            case PAGE_HOME:
                return new HomeFragment();
            case PAGE_ADD_HABIT:
                return new AddHabitFragment();
            case PAGE_EDIT_HABITS:
                return new EditHabitsFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
