package de.idrinth.habitevaluator.android.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.idrinth.habitevaluator.android.AddEmotionPairFragment;
import de.idrinth.habitevaluator.android.AddHabitFragment;
import de.idrinth.habitevaluator.android.DiaryFragment;
import de.idrinth.habitevaluator.android.DiaryNavigationFragment;
import de.idrinth.habitevaluator.android.EditHabitsFragment;
import de.idrinth.habitevaluator.android.EmotionalStateFragment;
import de.idrinth.habitevaluator.android.HomeFragment;
import de.idrinth.habitevaluator.android.ImprintFragment;
import de.idrinth.habitevaluator.android.PointDevelopmentFragment;
import de.idrinth.habitevaluator.android.RecordEmotionEntryFragment;
import de.idrinth.habitevaluator.android.SettingsFragment;
import de.idrinth.habitevaluator.android.SleepTrackingFragment;
import de.idrinth.habitevaluator.android.SportLogFragment;
import de.idrinth.habitevaluator.android.FoodLogFragment;
import de.idrinth.habitevaluator.android.MedicationListFragment;
import de.idrinth.habitevaluator.android.MedicationLogFragment;
import de.idrinth.habitevaluator.android.EmergencyPlanFragment;
import de.idrinth.habitevaluator.android.ActivityLogFragment;
import de.idrinth.habitevaluator.android.StatsFragment;

public class ScreenPagerAdapter extends FragmentStateAdapter {

    // Bottom navigation pages (swipeable, must match bottom_navigation.xml order)
    public static final int PAGE_HOME = 0;
    public static final int PAGE_DIARY = 1;
    public static final int PAGE_SLEEP = 2;
    public static final int PAGE_EMERGENCY_PLAN = 3;
    public static final int PAGE_EMOTIONAL_STATE = 4;
    public static final int LAST_SWIPEABLE_PAGE = PAGE_EMOTIONAL_STATE;

    // Programmatic-only pages (not reachable via swiping)
    public static final int PAGE_EDIT_HABITS = 5;
    public static final int PAGE_ADD_HABIT = 6;
    public static final int PAGE_STATS = 7;
    public static final int PAGE_POINT_DEVELOPMENT = 8;
    public static final int PAGE_ADD_EMOTION_PAIR = 9;
    public static final int PAGE_RECORD_EMOTION_ENTRY = 10;
    public static final int PAGE_SETTINGS = 11;
    public static final int PAGE_IMPRINT = 12;
    public static final int PAGE_POSITIVITY_DIARY = 13;
    public static final int PAGE_SPORT_LOG = 14;
    public static final int PAGE_FOOD_LOG = 15;
    public static final int PAGE_MEDICATION_LOG = 16;
    public static final int PAGE_MEDICATION_LIST = 17;
    public static final int PAGE_ACTIVITY_LOG = 18;
    public static final int PAGE_COUNT = 19;

    public ScreenPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case PAGE_HOME:
                return new HomeFragment();
            case PAGE_DIARY:
                return new DiaryNavigationFragment();
            case PAGE_SLEEP:
                return new SleepTrackingFragment();
            case PAGE_EMERGENCY_PLAN:
                return new EmergencyPlanFragment();
            case PAGE_EMOTIONAL_STATE:
                return new EmotionalStateFragment();
            case PAGE_EDIT_HABITS:
                return new EditHabitsFragment();
            case PAGE_ADD_HABIT:
                return new AddHabitFragment();
            case PAGE_STATS:
                return new StatsFragment();
            case PAGE_POINT_DEVELOPMENT:
                return new PointDevelopmentFragment();
            case PAGE_ADD_EMOTION_PAIR:
                return new AddEmotionPairFragment();
            case PAGE_RECORD_EMOTION_ENTRY:
                return new RecordEmotionEntryFragment();
            case PAGE_SETTINGS:
                return new SettingsFragment();
            case PAGE_IMPRINT:
                return new ImprintFragment();
            case PAGE_POSITIVITY_DIARY:
                return new DiaryFragment();
            case PAGE_SPORT_LOG:
                return new SportLogFragment();
            case PAGE_FOOD_LOG:
                return new FoodLogFragment();
            case PAGE_MEDICATION_LOG:
                return new MedicationLogFragment();
            case PAGE_MEDICATION_LIST:
                return new MedicationListFragment();
            case PAGE_ACTIVITY_LOG:
                return new ActivityLogFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
