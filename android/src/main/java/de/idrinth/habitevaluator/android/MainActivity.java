package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.ActivityMainBinding;
import de.idrinth.habitevaluator.shared.persistence.FileSystemDiaryEntryRepository;
import de.idrinth.habitevaluator.shared.persistence.FileSystemEmotionEntryRepository;
import de.idrinth.habitevaluator.shared.persistence.FileSystemEmotionPairRepository;
import de.idrinth.habitevaluator.shared.persistence.FileSystemHabitCategoryRepository;
import de.idrinth.habitevaluator.shared.persistence.FileSystemHabitRepository;
import de.idrinth.habitevaluator.shared.persistence.FileSystemSleepEntryRepository;
import de.idrinth.habitevaluator.android.persistence.JsonToSqliteMigration;
import de.idrinth.habitevaluator.android.persistence.SQLiteDiaryEntryRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteDiaryReferenceRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteEmotionEntryRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteEmotionPairRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteHabitCategoryRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteHabitRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteHelper;
import de.idrinth.habitevaluator.android.persistence.SQLiteSleepEntryRepository;
import de.idrinth.habitevaluator.android.persistence.SQLiteSportLogRepository;
import de.idrinth.habitevaluator.android.ui.ScreenPagerAdapter;
import de.idrinth.habitevaluator.android.ui.ViewPager2SwipeSensitivityReducer;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.api.SyncService;
import de.idrinth.habitevaluator.shared.api.VersionMismatchException;
import de.idrinth.habitevaluator.shared.backup.BackupException;
import de.idrinth.habitevaluator.shared.backup.BackupService;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(FontSizeHelper.applyFontScale(newBase));
    }

    private static final String PLACEHOLDER_USERNAME = "android_user";
    private static List<Habit> sharedHabits;
    private static HabitRepository sharedHabitRepository;
    private static boolean sharedUsingRemoteStorage;
    private static List<HabitCategory> sharedCategories = new ArrayList<>();
    private static HabitCategoryRepository sharedCategoryRepository;
    private static ApiClient sharedApiClient;
    private static User sharedCurrentUser;
    private static List<SleepEntry> sharedSleepEntries = new ArrayList<>();
    private static SleepEntryRepository sharedSleepEntryRepository;
    private static DiaryEntryRepository sharedDiaryEntryRepository;
    private static DiaryReferenceRepository sharedDiaryReferenceRepository;
    private static EmotionPairRepository sharedEmotionPairRepository;
    private static List<EmotionPair> sharedEmotionPairs = new ArrayList<>();
    private static EmotionEntryRepository sharedEmotionEntryRepository;
    private static SportLogRepository sharedSportLogRepository;
    private static de.idrinth.habitevaluator.shared.repository.FoodLogRepository sharedFoodLogRepository;
    private static de.idrinth.habitevaluator.android.persistence.SQLiteFoodTagRepository sharedFoodTagRepository;
    private static de.idrinth.habitevaluator.shared.repository.MedicationRepository sharedMedicationRepository;
    private static de.idrinth.habitevaluator.shared.repository.MedicationLogRepository sharedMedicationLogRepository;
    private static List<de.idrinth.habitevaluator.shared.model.Medication> sharedMedications = new ArrayList<>();
    private static de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository sharedEmergencyPlanStepRepository;
    private static de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository sharedEmergencyPlanActionRepository;
    private static de.idrinth.habitevaluator.shared.repository.ActivityLogRepository sharedActivityLogRepository;
    private static User sharedLocalUser;
    private static String editHabitId;
    private static String pointDevelopmentHabitId;
    private static String recordEmotionPairId;

    public static List<SleepEntry> getSharedSleepEntries() {
        return sharedSleepEntries;
    }

    public static SleepEntryRepository getSharedSleepEntryRepository() {
        return sharedSleepEntryRepository;
    }

    public static List<Habit> getSharedHabits() {
        return sharedHabits;
    }

    public static HabitRepository getSharedHabitRepository() {
        return sharedHabitRepository;
    }

    public static boolean isSharedUsingRemoteStorage() {
        return sharedUsingRemoteStorage;
    }

    public static List<HabitCategory> getSharedCategories() {
        return sharedCategories;
    }

    public static HabitCategoryRepository getSharedCategoryRepository() {
        return sharedCategoryRepository;
    }

    public static ApiClient getSharedApiClient() {
        return sharedApiClient;
    }

    public static User getSharedCurrentUser() {
        return sharedCurrentUser;
    }

    /**
     * Returns the local user for local-only data operations (diary, sleep, emotions, sport, food).
     * In local storage mode this is the same as getSharedCurrentUser().
     * In remote storage mode this returns the local user whose ID matches entries in the local SQLite database,
     * rather than the remote API user.
     */
    public static User getSharedLocalUser() {
        return sharedLocalUser;
    }

    public static DiaryEntryRepository getSharedDiaryEntryRepository() {
        return sharedDiaryEntryRepository;
    }

    public static DiaryReferenceRepository getSharedDiaryReferenceRepository() {
        return sharedDiaryReferenceRepository;
    }

    public static EmotionPairRepository getSharedEmotionPairRepository() {
        return sharedEmotionPairRepository;
    }

    public static List<EmotionPair> getSharedEmotionPairs() {
        return sharedEmotionPairs;
    }

    public static String getEditHabitId() {
        return editHabitId;
    }

    public static void setEditHabitId(String habitId) {
        editHabitId = habitId;
    }

    public static String getPointDevelopmentHabitId() {
        return pointDevelopmentHabitId;
    }

    public static void setPointDevelopmentHabitId(String habitId) {
        pointDevelopmentHabitId = habitId;
    }

    public static String getRecordEmotionPairId() {
        return recordEmotionPairId;
    }

    public static void setRecordEmotionPairId(String pairId) {
        recordEmotionPairId = pairId;
    }

    public static EmotionEntryRepository getSharedEmotionEntryRepository() {
        return sharedEmotionEntryRepository;
    }

    public static SportLogRepository getSharedSportLogRepository() {
        return sharedSportLogRepository;
    }

    public static de.idrinth.habitevaluator.shared.repository.FoodLogRepository getSharedFoodLogRepository() {
        return sharedFoodLogRepository;
    }

    public static de.idrinth.habitevaluator.android.persistence.SQLiteFoodTagRepository getSharedFoodTagRepository() {
        return sharedFoodTagRepository;
    }

    public static de.idrinth.habitevaluator.shared.repository.MedicationRepository getSharedMedicationRepository() {
        return sharedMedicationRepository;
    }

    public static de.idrinth.habitevaluator.shared.repository.MedicationLogRepository getSharedMedicationLogRepository() {
        return sharedMedicationLogRepository;
    }

    public static List<de.idrinth.habitevaluator.shared.model.Medication> getSharedMedications() {
        return sharedMedications;
    }

    public static void refreshSharedMedications() {
        sharedMedications.clear();
        if (sharedMedicationRepository != null && sharedLocalUser != null) {
            sharedMedications.addAll(sharedMedicationRepository.findByUserId(sharedLocalUser.getId()));
        }
    }

    public static de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository getSharedEmergencyPlanStepRepository() {
        return sharedEmergencyPlanStepRepository;
    }

    public static de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository getSharedEmergencyPlanActionRepository() {
        return sharedEmergencyPlanActionRepository;
    }

    public static de.idrinth.habitevaluator.shared.repository.ActivityLogRepository getSharedActivityLogRepository() {
        return sharedActivityLogRepository;
    }

    public static void saveAllHabits() {
        if (sharedHabitRepository == null || sharedHabits == null) {
            return;
        }
        if (sharedUsingRemoteStorage) {
            new Thread(() -> {
                for (Habit habit : sharedHabits) {
                    sharedHabitRepository.save(habit);
                }
            }).start();
        } else {
            for (Habit habit : sharedHabits) {
                sharedHabitRepository.save(habit);
            }
        }
    }

    private ActivityMainBinding binding;
    private List<Habit> habits;
    private User currentUser;
    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private ApiClient apiClient;
    private boolean usingRemoteStorage;
    private HabitRepository localBackupRepository;
    private User localBackupUser;
    private List<HabitCategory> categoryList = new ArrayList<>();
    private SleepEntryRepository sleepEntryRepository;
    private List<SleepEntry> sleepEntries = new ArrayList<>();
    private int programmaticNavigationTarget = -1;
    private boolean isSyncingBottomNav = false;
    private final BackupService backupServiceInstance = new BackupService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences themePrefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        SettingsActivity.applyThemeMode(themePrefs.getString(SettingsActivity.KEY_THEME_MODE, SettingsActivity.THEME_SYSTEM));
        SettingsActivity.applyLanguage(themePrefs.getString(SettingsActivity.KEY_LANGUAGE, SettingsActivity.LANGUAGE_SYSTEM));

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = new ArrayList<>();
        sharedHabits = habits;

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(SettingsActivity.KEY_FIRST_START_COMPLETED, false)) {
            binding.welcomeOverlay.setVisibility(android.view.View.VISIBLE);
            binding.welcomeMessage.setText(getString(R.string.first_start_not_professional_help)
                    + "\n\n"
                    + getString(R.string.first_start_no_data_sharing));
            binding.welcomeAcknowledgeButton.setOnClickListener(v -> {
                prefs.edit().putBoolean(SettingsActivity.KEY_FIRST_START_COMPLETED, true).apply();
                binding.welcomeOverlay.setVisibility(android.view.View.GONE);
                initializeStorage();
                completeSetup();
            });
        } else {
            initializeStorage();
            completeSetup();
        }
    }

    private void completeSetup() {
        setupViewPager();
        setupBottomNavigation();
        setupStatsButton();
        setupSettingsButton();
        setupImprintButton();
        performDailyBackupIfEnabled();
    }

    public void navigateToEditHabit(String habitId) {
        setEditHabitId(habitId);
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EDIT_HABITS;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EDIT_HABITS, true);
    }

    public void navigateToPointDevelopment(String habitId) {
        setPointDevelopmentHabitId(habitId);
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_POINT_DEVELOPMENT, true);
    }

    public void navigateToHome() {
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, true);
    }

    private void setupViewPager() {
        ScreenPagerAdapter pagerAdapter = new ScreenPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, false);
        ViewPager2SwipeSensitivityReducer.reduce(binding.viewPager, 3);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                boolean isProgrammatic = (programmaticNavigationTarget == position);
                if (isProgrammatic) {
                    programmaticNavigationTarget = -1;
                }
                boolean isIntermediate = (programmaticNavigationTarget >= 0);
                isSyncingBottomNav = true;

                switch (position) {
                    // Bottom navigation pages (swipeable)
                    case ScreenPagerAdapter.PAGE_HOME:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                        break;
                    case ScreenPagerAdapter.PAGE_DIARY:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_diary);
                        androidx.fragment.app.Fragment diaryFrag = getSupportFragmentManager()
                                .findFragmentByTag("f" + ScreenPagerAdapter.PAGE_DIARY);
                        if (diaryFrag instanceof DiaryNavigationFragment) {
                            ((DiaryNavigationFragment) diaryFrag).scrollToTop();
                        }
                        break;
                    case ScreenPagerAdapter.PAGE_SLEEP:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_sleep);
                        break;
                    case ScreenPagerAdapter.PAGE_EMERGENCY_PLAN:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_emergency_plan);
                        break;
                    case ScreenPagerAdapter.PAGE_EMOTIONAL_STATE:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_emotions);
                        androidx.fragment.app.Fragment emotionFrag = getSupportFragmentManager()
                                .findFragmentByTag("f" + ScreenPagerAdapter.PAGE_EMOTIONAL_STATE);
                        if (emotionFrag instanceof EmotionalStateFragment) {
                            ((EmotionalStateFragment) emotionFrag).refreshData();
                        }
                        break;

                    // Diary sub-pages (programmatic only, show diary in bottom nav)
                    case ScreenPagerAdapter.PAGE_POSITIVITY_DIARY:
                    case ScreenPagerAdapter.PAGE_SPORT_LOG:
                    case ScreenPagerAdapter.PAGE_FOOD_LOG:
                    case ScreenPagerAdapter.PAGE_MEDICATION_LOG:
                    case ScreenPagerAdapter.PAGE_MEDICATION_LIST:
                    case ScreenPagerAdapter.PAGE_ACTIVITY_LOG:
                        if (isProgrammatic) {
                            binding.bottomNavigation.setSelectedItemId(R.id.nav_diary);
                        } else if (!isIntermediate) {
                            binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_DIARY, false);
                        }
                        break;

                    // Emotion sub-pages (programmatic only)
                    case ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR:
                    case ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY:
                        if (!isProgrammatic && !isIntermediate) {
                            binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMOTIONAL_STATE, false);
                        }
                        break;

                    // All other programmatic-only pages
                    default:
                        if (!isProgrammatic && !isIntermediate) {
                            binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, false);
                        }
                        break;
                }
                isSyncingBottomNav = false;
            }
        });
    }

    private void setupBottomNavigation() {
        applyModuleVisibility();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (isSyncingBottomNav) {
                return true;
            }
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_HOME;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, true);
                return true;
            } else if (id == R.id.nav_diary) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_DIARY;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_DIARY, true);
                return true;
            } else if (id == R.id.nav_emotions) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EMOTIONAL_STATE;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMOTIONAL_STATE, true);
                return true;
            } else if (id == R.id.nav_sleep) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_SLEEP;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_SLEEP, true);
                return true;
            } else if (id == R.id.nav_emergency_plan) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EMERGENCY_PLAN;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMERGENCY_PLAN, true);
                return true;
            }
            return false;
        });
        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            if (isSyncingBottomNav) {
                return;
            }
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_HOME;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, true);
            } else if (id == R.id.nav_diary) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_DIARY;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_DIARY, true);
            } else if (id == R.id.nav_emotions) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EMOTIONAL_STATE;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMOTIONAL_STATE, true);
            } else if (id == R.id.nav_sleep) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_SLEEP;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_SLEEP, true);
            } else if (id == R.id.nav_emergency_plan) {
                programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EMERGENCY_PLAN;
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMERGENCY_PLAN, true);
            }
        });
    }



    private void setupStatsButton() {
        binding.statsButton.setOnClickListener(v -> {
            programmaticNavigationTarget = ScreenPagerAdapter.PAGE_STATS;
            binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_STATS, true);
        });
    }

    private void setupSettingsButton() {
        binding.settingsButton.setOnClickListener(v -> {
            programmaticNavigationTarget = ScreenPagerAdapter.PAGE_SETTINGS;
            binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_SETTINGS, true);
        });
    }

    private void setupImprintButton() {
        binding.imprintButton.setOnClickListener(v -> {
            programmaticNavigationTarget = ScreenPagerAdapter.PAGE_IMPRINT;
            binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_IMPRINT, true);
        });
    }

    public void navigateToAddHabit() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_ADD_HABIT;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_ADD_HABIT, true);
    }

    public void navigateToAddEmotionPair() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_ADD_EMOTION_PAIR, true);
    }

    public void navigateToRecordEmotionEntry(String pairId) {
        setRecordEmotionPairId(pairId);
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_RECORD_EMOTION_ENTRY, true);
    }

    public void navigateToEmotionalState() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EMOTIONAL_STATE;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMOTIONAL_STATE, true);
    }

    public void navigateToImprint() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_IMPRINT;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_IMPRINT, true);
    }

    public void navigateToPositivityDiary() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_POSITIVITY_DIARY;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_POSITIVITY_DIARY, true);
    }

    public void navigateToSportLog() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_SPORT_LOG;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_SPORT_LOG, true);
    }

    public void navigateToFoodLog() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_FOOD_LOG;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_FOOD_LOG, true);
    }

    public void navigateToMedicationLog() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_MEDICATION_LOG;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_MEDICATION_LOG, true);
    }

    public void navigateToMedicationList() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_MEDICATION_LIST;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_MEDICATION_LIST, true);
    }

    public void navigateToEmergencyPlan() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_EMERGENCY_PLAN;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EMERGENCY_PLAN, true);
    }

    public void navigateToActivityLog() {
        programmaticNavigationTarget = ScreenPagerAdapter.PAGE_ACTIVITY_LOG;
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_ACTIVITY_LOG, true);
    }

    public void onSettingsChanged() {
        initializeStorage();
        applyModuleVisibility();
    }

    private void applyModuleVisibility() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean diaryVisible = prefs.getBoolean(SettingsActivity.KEY_MODULE_DIARY_VISIBLE, true);
        boolean sleepVisible = prefs.getBoolean(SettingsActivity.KEY_MODULE_SLEEP_VISIBLE, true);
        boolean statisticsVisible = prefs.getBoolean(SettingsActivity.KEY_MODULE_STATISTICS_VISIBLE, true);
        boolean emotionsVisible = prefs.getBoolean(SettingsActivity.KEY_MODULE_EMOTIONS_VISIBLE, true);
        android.view.Menu menu = binding.bottomNavigation.getMenu();
        menu.findItem(R.id.nav_diary).setVisible(diaryVisible);
        menu.findItem(R.id.nav_sleep).setVisible(sleepVisible);
        binding.statsButton.setVisibility(statisticsVisible ? android.view.View.VISIBLE : android.view.View.GONE);
        menu.findItem(R.id.nav_emotions).setVisible(emotionsVisible);
    }

    private void initializeStorage() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(SettingsActivity.KEY_STORAGE_MODE, SettingsActivity.MODE_LOCAL);

        if (SettingsActivity.MODE_REMOTE.equals(mode)) {
            initializeRemoteStorage(prefs);
        } else {
            initializeLocalStorage();
        }
        updateStorageModeLabel();
        loadCategories();
    }

    private void initializeLocalStorage() {
        usingRemoteStorage = false;
        localBackupRepository = null;
        localBackupUser = null;
        apiClient = null;

        SQLiteHelper dbHelper = SQLiteHelper.getInstance(this);

        habitRepository = new SQLiteHabitRepository(dbHelper);
        categoryRepository = new SQLiteHabitCategoryRepository(dbHelper);
        sleepEntryRepository = new SQLiteSleepEntryRepository(dbHelper);
        SQLiteDiaryReferenceRepository diaryRefRepo = new SQLiteDiaryReferenceRepository(dbHelper);
        SQLiteDiaryEntryRepository diaryEntryRepo = new SQLiteDiaryEntryRepository(dbHelper);
        diaryEntryRepo.setDiaryReferenceRepository(diaryRefRepo);
        SQLiteEmotionPairRepository emotionPairRepo = new SQLiteEmotionPairRepository(dbHelper);
        SQLiteEmotionEntryRepository emotionEntryRepo = new SQLiteEmotionEntryRepository(dbHelper, emotionPairRepo);
        SQLiteSportLogRepository sportLogRepo = new SQLiteSportLogRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteFoodLogRepository foodLogRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteFoodLogRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteFoodTagRepository foodTagRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteFoodTagRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteMedicationRepository medicationRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteMedicationRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteMedicationLogRepository medicationLogRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteMedicationLogRepository(dbHelper, medicationRepo);
        de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanActionRepository emergencyPlanActionRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanActionRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanStepRepository emergencyPlanStepRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanStepRepository(dbHelper, emergencyPlanActionRepo);
        de.idrinth.habitevaluator.android.persistence.SQLiteActivityLogRepository activityLogRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteActivityLogRepository(dbHelper);

        currentUser = getOrCreateLocalUser();
        sharedHabitRepository = habitRepository;
        sharedUsingRemoteStorage = false;
        sharedCategoryRepository = categoryRepository;
        sharedSleepEntryRepository = sleepEntryRepository;
        sharedApiClient = null;
        sharedCurrentUser = currentUser;
        sharedLocalUser = currentUser;
        sharedDiaryEntryRepository = diaryEntryRepo;
        sharedDiaryReferenceRepository = diaryRefRepo;
        sharedEmotionPairRepository = emotionPairRepo;
        sharedEmotionEntryRepository = emotionEntryRepo;
        sharedSportLogRepository = sportLogRepo;
        sharedFoodLogRepository = foodLogRepo;
        sharedFoodTagRepository = foodTagRepo;
        sharedMedicationRepository = medicationRepo;
        sharedMedicationLogRepository = medicationLogRepo;
        sharedEmergencyPlanStepRepository = emergencyPlanStepRepo;
        sharedEmergencyPlanActionRepository = emergencyPlanActionRepo;
        sharedActivityLogRepository = activityLogRepo;

        // Migrate legacy JSON files to SQLite if they exist
        java.io.File storageDir = new java.io.File(getFilesDir(), "habit-data");
        JsonToSqliteMigration migration = new JsonToSqliteMigration(
                storageDir,
                habitRepository,
                categoryRepository,
                diaryRefRepo,
                diaryEntryRepo,
                sleepEntryRepository,
                emotionPairRepo,
                emotionEntryRepo
        );
        if (migration.needsMigration()) {
            migration.migrate();
        }

        loadSleepEntries();
        loadEmotionPairs();
        loadMedications();
    }

    private void loadMedications() {
        sharedMedications.clear();
        if (sharedMedicationRepository != null && sharedLocalUser != null) {
            sharedMedications.addAll(sharedMedicationRepository.findByUserId(sharedLocalUser.getId()));
        }
    }

    private User getOrCreateLocalUser() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString("local_user_id", null);
        User user = new User(PLACEHOLDER_USERNAME, "placeholder");
        if (userId != null) {
            user.setId(userId);
        } else {
            prefs.edit().putString("local_user_id", user.getId()).apply();
        }
        return user;
    }

    private void initializeRemoteStorage(SharedPreferences prefs) {
        String url = prefs.getString(SettingsActivity.KEY_API_URL, "");
        String username = prefs.getString(SettingsActivity.KEY_API_USERNAME, "");
        String password = prefs.getString(SettingsActivity.KEY_API_PASSWORD, "");

        // Initialize local-only repositories (no remote implementations exist for these)
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(this);
        SQLiteHabitRepository localHabitRepo = new SQLiteHabitRepository(dbHelper);
        SQLiteHabitCategoryRepository localCategoryRepo = new SQLiteHabitCategoryRepository(dbHelper);
        sleepEntryRepository = new SQLiteSleepEntryRepository(dbHelper);
        SQLiteDiaryReferenceRepository diaryRefRepo = new SQLiteDiaryReferenceRepository(dbHelper);
        SQLiteDiaryEntryRepository diaryEntryRepo = new SQLiteDiaryEntryRepository(dbHelper);
        diaryEntryRepo.setDiaryReferenceRepository(diaryRefRepo);
        SQLiteEmotionPairRepository emotionPairRepo = new SQLiteEmotionPairRepository(dbHelper);
        SQLiteEmotionEntryRepository emotionEntryRepo = new SQLiteEmotionEntryRepository(dbHelper, emotionPairRepo);
        SQLiteSportLogRepository sportLogRepo = new SQLiteSportLogRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteFoodLogRepository foodLogRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteFoodLogRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteFoodTagRepository foodTagRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteFoodTagRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteMedicationRepository medicationRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteMedicationRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteMedicationLogRepository medicationLogRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteMedicationLogRepository(dbHelper, medicationRepo);
        de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanActionRepository emergencyPlanActionRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanActionRepository(dbHelper);
        de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanStepRepository emergencyPlanStepRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteEmergencyPlanStepRepository(dbHelper, emergencyPlanActionRepo);
        de.idrinth.habitevaluator.android.persistence.SQLiteActivityLogRepository activityLogRepo = new de.idrinth.habitevaluator.android.persistence.SQLiteActivityLogRepository(dbHelper);
        sharedSleepEntryRepository = sleepEntryRepository;
        sharedDiaryEntryRepository = diaryEntryRepo;
        sharedDiaryReferenceRepository = diaryRefRepo;
        sharedEmotionPairRepository = emotionPairRepo;
        sharedEmotionEntryRepository = emotionEntryRepo;
        sharedSportLogRepository = sportLogRepo;
        sharedFoodLogRepository = foodLogRepo;
        sharedFoodTagRepository = foodTagRepo;
        sharedMedicationRepository = medicationRepo;
        sharedMedicationLogRepository = medicationLogRepo;
        sharedEmergencyPlanStepRepository = emergencyPlanStepRepo;
        sharedEmergencyPlanActionRepository = emergencyPlanActionRepo;
        sharedActivityLogRepository = activityLogRepo;

        // Set local user for local-only data access (diary, sleep, emotions, sport, food, medication, emergency plan, activity log)
        sharedLocalUser = getOrCreateLocalUser();

        // Migrate legacy JSON files to SQLite if they exist
        java.io.File migrationDir = new java.io.File(getFilesDir(), "habit-data");
        JsonToSqliteMigration migration = new JsonToSqliteMigration(
                migrationDir,
                localHabitRepo,
                localCategoryRepo,
                diaryRefRepo,
                diaryEntryRepo,
                sleepEntryRepository,
                emotionPairRepo,
                emotionEntryRepo
        );
        if (migration.needsMigration()) {
            migration.migrate();
        }

        new Thread(() -> {
            try {
                ApiClient client = new ApiClient(url);
                boolean loggedIn = client.login(username, password);
                if (loggedIn) {
                    apiClient = client;
                    habitRepository = new RemoteHabitRepository(client);
                    RemoteUserRepository userRepo = new RemoteUserRepository(apiClient);
                    currentUser = userRepo.findAll().stream().findFirst().orElse(null);
                    usingRemoteStorage = true;
                    sharedHabitRepository = habitRepository;
                    sharedUsingRemoteStorage = true;
                    sharedCategoryRepository = null;
                    sharedApiClient = apiClient;
                    sharedCurrentUser = currentUser;

                    // Initialize local backup for data safety
                    java.io.File storageDir = new java.io.File(getFilesDir(), "habit-data");
                    localBackupRepository = new FileSystemHabitRepository(storageDir);
                    localBackupUser = getOrCreateLocalUser();

                    // Sync remote data to local backup on start
                    syncOnStart(url, username, password);

                    runOnUiThread(() -> {
                        updateStorageModeLabel();
                        loadCategories();
                        loadHabits();
                        loadSleepEntries();
                        loadEmotionPairs();
                        loadMedications();
                    });
                    return;
                }
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to connect to remote API, using local storage",
                                Toast.LENGTH_LONG).show());
            }
            runOnUiThread(() -> {
                initializeLocalStorage();
                updateStorageModeLabel();
                loadCategories();
                loadHabits();
            });
        }).start();
    }

    private void syncOnStart(String url, String username, String password) {
        if (localBackupRepository == null || localBackupUser == null) {
            return;
        }
        try {
            SyncService syncService = new SyncService(localBackupRepository);
            syncService.sync(url, username, password, localBackupUser, BuildConfig.VERSION_NAME);
        } catch (VersionMismatchException e) {
            runOnUiThread(() -> Toast.makeText(this,
                    "Version mismatch: your version (" + e.getClientVersion()
                            + ") does not match server (" + e.getServerVersion()
                            + "). Please update before syncing.",
                    Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            // Sync failure on start is non-fatal; remote data will still be used
        }
    }

    private void loadCategories() {
        categoryList.clear();
        if (usingRemoteStorage && apiClient != null) {
            new Thread(() -> {
                try {
                    List<HabitCategory> remoteCats = apiClient.get("/api/categories",
                            new TypeToken<List<HabitCategory>>() {}.getType());
                    runOnUiThread(() -> {
                        if (remoteCats != null) {
                            categoryList.addAll(remoteCats);
                        }
                        syncSharedCategories();
                        loadHabits();
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        syncSharedCategories();
                        loadHabits();
                    });
                }
            }).start();
        } else if (categoryRepository != null && currentUser != null) {
            categoryList.addAll(categoryRepository.findByUserId(currentUser.getId()));
            syncSharedCategories();
            loadHabits();
        } else {
            syncSharedCategories();
            loadHabits();
        }
    }

    private void syncSharedCategories() {
        sharedCategories.clear();
        sharedCategories.addAll(categoryList);
        sharedCategories.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }

    private void loadHabits() {
        habits.clear();
        if (habitRepository != null && currentUser != null) {
            if (usingRemoteStorage) {
                new Thread(() -> {
                    List<Habit> remoteHabits = habitRepository.findByUserId(currentUser.getId());
                    runOnUiThread(() -> habits.addAll(remoteHabits));
                }).start();
            } else {
                habits.addAll(habitRepository.findByUserId(currentUser.getId()));
            }
        }
    }

    private void updateStorageModeLabel() {
        if (usingRemoteStorage) {
            binding.storageModeText.setText(R.string.storage_mode_remote);
        } else {
            binding.storageModeText.setText(R.string.storage_mode_local);
        }
    }

    private void loadSleepEntries() {
        sleepEntries.clear();
        sharedSleepEntries.clear();
        if (sleepEntryRepository != null && sharedLocalUser != null) {
            sleepEntries.addAll(sleepEntryRepository.findByUserId(sharedLocalUser.getId()));
            sharedSleepEntries.addAll(sleepEntries);
        }
    }

    private void loadEmotionPairs() {
        sharedEmotionPairs.clear();
        if (sharedEmotionPairRepository != null && sharedLocalUser != null) {
            sharedEmotionPairs.addAll(sharedEmotionPairRepository.findByUserId(sharedLocalUser.getId()));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        performDailyBackupIfEnabled();
        if (usingRemoteStorage && localBackupRepository != null && localBackupUser != null) {
            new Thread(() -> {
                // Save current habits to local backup
                for (Habit habit : habits) {
                    Habit backupCopy = copyHabitForBackup(habit, localBackupUser);
                    localBackupRepository.save(backupCopy);
                }
                // Sync local backup with remote to push any final changes
                SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
                String url = prefs.getString(SettingsActivity.KEY_API_URL, "");
                String username = prefs.getString(SettingsActivity.KEY_API_USERNAME, "");
                String password = prefs.getString(SettingsActivity.KEY_API_PASSWORD, "");
                try {
                    SyncService syncService = new SyncService(localBackupRepository);
                    syncService.sync(url, username, password, localBackupUser, BuildConfig.VERSION_NAME);
                } catch (VersionMismatchException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Sync skipped: version mismatch with server.",
                            Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    // Best effort sync on stop; local backup is already saved
                }
            }).start();
        }
    }

    private Habit copyHabitForBackup(Habit source, User backupUser) {
        Habit habit = new Habit(source.getName(), source.getDescription());
        habit.setFrequencyType(source.getFrequencyType());
        habit.setTargetFrequency(source.getTargetFrequency());
        habit.setMaxEntriesPerDay(source.getMaxEntriesPerDay());
        habit.setCategoryId(source.getCategoryId());
        habit.setPositiveScoring(source.isPositiveScoring());
        habit.setScoringRule(source.getScoringRule());
        habit.setUser(backupUser);
        for (HabitEntry sourceEntry : source.getEntries()) {
            HabitEntry entry = new HabitEntry();
            entry.setId(sourceEntry.getId());
            entry.setCompletedAt(sourceEntry.getCompletedAt());
            entry.setNotes(sourceEntry.getNotes());
            entry.setValue(sourceEntry.getValue());
            habit.addEntry(entry);
        }
        return habit;
    }

    private void performDailyBackupIfEnabled() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean backupEnabled = prefs.getBoolean(SettingsActivity.KEY_BACKUP_ENABLED, false);
        if (!backupEnabled) {
            return;
        }
        String password = prefs.getString(SettingsActivity.KEY_BACKUP_PASSWORD, "");
        if (password.isEmpty()) {
            return;
        }
        String backupLocationUri = prefs.getString(SettingsActivity.KEY_BACKUP_LOCATION_URI, "");
        if (!backupLocationUri.isEmpty()) {
            performDailyBackupToSaf(android.net.Uri.parse(backupLocationUri), password);
        } else {
            performDailyBackupToInternal(password);
        }
    }

    private void performDailyBackupToInternal(String password) {
        java.io.File backupDir = new java.io.File(getFilesDir(), "backups");
        if (backupServiceInstance.hasTodaysBackup(backupDir)) {
            return;
        }
        new Thread(() -> {
            try {
                backupServiceInstance.createBackup(backupDir, password, currentUser,
                        habitRepository, categoryRepository,
                        sharedDiaryEntryRepository, sleepEntryRepository);
            } catch (BackupException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Backup failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void performDailyBackupToSaf(android.net.Uri treeUri, String password) {
        new Thread(() -> {
            try {
                androidx.documentfile.provider.DocumentFile treeDoc =
                        androidx.documentfile.provider.DocumentFile.fromTreeUri(this, treeUri);
                if (treeDoc == null) {
                    return;
                }
                String todaysFilename = backupServiceInstance.getTodaysBackupFilename();
                androidx.documentfile.provider.DocumentFile existing = treeDoc.findFile(todaysFilename);
                if (existing != null) {
                    return;
                }
                byte[] backupBytes = backupServiceInstance.createBackupBytes(password, currentUser,
                        habitRepository, categoryRepository,
                        sharedDiaryEntryRepository, sleepEntryRepository);
                androidx.documentfile.provider.DocumentFile newFile =
                        treeDoc.createFile("application/octet-stream", todaysFilename);
                if (newFile == null) {
                    return;
                }
                java.io.OutputStream outputStream = getContentResolver().openOutputStream(newFile.getUri());
                if (outputStream != null) {
                    outputStream.write(backupBytes);
                    outputStream.close();
                }
                cleanupOldSafBackups(treeDoc);
            } catch (BackupException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Backup failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            } catch (java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Backup failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void cleanupOldSafBackups(androidx.documentfile.provider.DocumentFile treeDoc) {
        java.util.List<androidx.documentfile.provider.DocumentFile> backups = new java.util.ArrayList<>();
        for (androidx.documentfile.provider.DocumentFile file : treeDoc.listFiles()) {
            if (file.isFile() && file.getName() != null && file.getName().endsWith(".backup")) {
                backups.add(file);
            }
        }
        backups.sort((a, b) -> {
            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";
            return nameB.compareTo(nameA);
        });
        while (backups.size() > 30) {
            backups.remove(backups.size() - 1).delete();
        }
    }

    public void loadDefaults() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
                String languageSetting = prefs.getString(SettingsActivity.KEY_LANGUAGE, SettingsActivity.LANGUAGE_SYSTEM);
                String language = SettingsActivity.getEffectiveLanguage(languageSetting);
                if (usingRemoteStorage && apiClient != null) {
                    apiClient.post("/api/init-defaults?language=" + language, Collections.emptyMap(),
                            new TypeToken<Map<String, Object>>() {}.getType());
                } else if (categoryRepository != null && habitRepository != null && currentUser != null) {
                    DefaultDataInitializer initializer = new DefaultDataInitializer(categoryRepository, habitRepository);
                    initializer.initializeDefaults(currentUser, language);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.defaults_loaded, Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, getString(R.string.load_defaults_failed, e.getMessage()),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
