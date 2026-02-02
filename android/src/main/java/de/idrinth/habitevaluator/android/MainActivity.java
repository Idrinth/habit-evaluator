package de.idrinth.habitevaluator.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.ActivityMainBinding;
import de.idrinth.habitevaluator.android.persistence.FileSystemHabitCategoryRepository;
import de.idrinth.habitevaluator.android.persistence.FileSystemHabitRepository;
import de.idrinth.habitevaluator.android.ui.HabitAdapter;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;

public class MainActivity extends AppCompatActivity implements HabitAdapter.OnHabitClickListener {

    private static final String PLACEHOLDER_USERNAME = "android_user";
    private static List<Habit> sharedHabits;
    private static HabitRepository sharedHabitRepository;
    private static boolean sharedUsingRemoteStorage;
    private static List<HabitCategory> sharedCategories = new ArrayList<>();
    private static HabitCategoryRepository sharedCategoryRepository;
    private static ApiClient sharedApiClient;
    private static User sharedCurrentUser;

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
    private HabitAdapter habitAdapter;
    private List<Habit> habits;
    private List<Habit> filteredHabits;
    private HabitEvaluatorService evaluatorService;
    private HabitScoringService scoringService;
    private Habit selectedHabit;
    private User currentUser;
    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private ApiClient apiClient;
    private boolean usingRemoteStorage;
    private List<HabitCategory> categoryList = new ArrayList<>();
    private final Map<String, String> categoryNameToId = new LinkedHashMap<>();

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    initializeStorage();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences themePrefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        SettingsActivity.applyThemeMode(themePrefs.getString(SettingsActivity.KEY_THEME_MODE, SettingsActivity.THEME_SYSTEM));

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = new ArrayList<>();
        filteredHabits = new ArrayList<>();
        sharedHabits = habits;
        evaluatorService = new HabitEvaluatorService();
        scoringService = new HabitScoringService();

        setupRecyclerView();
        initializeStorage();
        setupClickListeners();
        setupCategoryFilterSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFilter();
        updateLoadDefaultsButtonVisibility();
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
        java.io.File storageDir = new java.io.File(getFilesDir(), "habit-data");
        habitRepository = new FileSystemHabitRepository(storageDir);
        categoryRepository = new FileSystemHabitCategoryRepository(storageDir);
        apiClient = null;
        currentUser = getOrCreateLocalUser();
        sharedHabitRepository = habitRepository;
        sharedUsingRemoteStorage = false;
        sharedCategoryRepository = categoryRepository;
        sharedApiClient = null;
        sharedCurrentUser = currentUser;
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
                    runOnUiThread(() -> {
                        updateStorageModeLabel();
                        loadCategories();
                        loadHabits();
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

    private void loadCategories() {
        categoryList.clear();
        categoryNameToId.clear();
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
                        populateCategorySpinners();
                        loadHabits();
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        syncSharedCategories();
                        populateCategorySpinners();
                        loadHabits();
                    });
                }
            }).start();
        } else if (categoryRepository != null && currentUser != null) {
            categoryList.addAll(categoryRepository.findByUserId(currentUser.getId()));
            syncSharedCategories();
            populateCategorySpinners();
            loadHabits();
        } else {
            syncSharedCategories();
            populateCategorySpinners();
            loadHabits();
        }
    }

    private void syncSharedCategories() {
        sharedCategories.clear();
        sharedCategories.addAll(categoryList);
    }

    private void populateCategorySpinners() {
        categoryNameToId.clear();

        // Category filter spinner
        List<String> filterNames = new ArrayList<>();
        filterNames.add(getString(R.string.all_categories));
        for (HabitCategory cat : categoryList) {
            filterNames.add(cat.getName());
            categoryNameToId.put(cat.getName(), cat.getId());
        }
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterNames);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categoryFilterSpinner.setAdapter(filterAdapter);
    }

    private void setupCategoryFilterSpinner() {
        binding.categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        filteredHabits.clear();
        String selected = (String) binding.categoryFilterSpinner.getSelectedItem();
        String allCategories = getString(R.string.all_categories);

        if (selected == null || allCategories.equals(selected)) {
            filteredHabits.addAll(habits);
        } else {
            String categoryId = categoryNameToId.get(selected);
            if (categoryId != null) {
                for (Habit h : habits) {
                    if (categoryId.equals(h.getCategoryId())) {
                        filteredHabits.add(h);
                    }
                }
            }
        }
        habitAdapter.notifyDataSetChanged();
    }

    private void loadHabits() {
        habits.clear();
        if (habitRepository != null && currentUser != null) {
            if (usingRemoteStorage) {
                new Thread(() -> {
                    List<Habit> remoteHabits = habitRepository.findByUserId(currentUser.getId());
                    runOnUiThread(() -> {
                        habits.addAll(remoteHabits);
                        applyFilter();
                        updateLoadDefaultsButtonVisibility();
                    });
                }).start();
            } else {
                habits.addAll(habitRepository.findByUserId(currentUser.getId()));
            }
        }
        applyFilter();
        updateLoadDefaultsButtonVisibility();
    }

    private void updateLoadDefaultsButtonVisibility() {
        if (!categoryList.isEmpty() || !habits.isEmpty()) {
            binding.loadDefaultsButton.setVisibility(View.GONE);
        } else {
            binding.loadDefaultsButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateStorageModeLabel() {
        if (usingRemoteStorage) {
            binding.storageModeText.setText(R.string.storage_mode_remote);
        } else {
            binding.storageModeText.setText(R.string.storage_mode_local);
        }
    }

    private void setupRecyclerView() {
        habitAdapter = new HabitAdapter(filteredHabits, this);
        binding.habitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.habitsRecyclerView.setAdapter(habitAdapter);
    }

    private void setupClickListeners() {
        binding.addHabitButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddHabitActivity.class);
            startActivity(intent);
        });
        binding.completeButton.setOnClickListener(v -> completeHabit());
        binding.editHabitsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditHabitsActivity.class);
            startActivity(intent);
        });
        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });
        binding.loadDefaultsButton.setOnClickListener(v -> loadDefaults());
    }

    private void loadDefaults() {
        new Thread(() -> {
            try {
                if (usingRemoteStorage && apiClient != null) {
                    apiClient.post("/api/init-defaults", Collections.emptyMap(),
                            new TypeToken<Map<String, Object>>() {}.getType());
                } else if (categoryRepository != null && habitRepository != null && currentUser != null) {
                    DefaultDataInitializer initializer = new DefaultDataInitializer(categoryRepository, habitRepository);
                    initializer.initializeDefaults(currentUser);
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

    private void completeHabit() {
        if (selectedHabit == null) {
            Toast.makeText(this, "Please select a habit first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHabit.hasReachedDailyLimit(LocalDate.now())) {
            Toast.makeText(this, "Daily limit reached for this habit", Toast.LENGTH_SHORT).show();
            return;
        }

        HabitEntry entry = new HabitEntry(selectedHabit.getId());
        selectedHabit.addEntry(entry);

        if (habitRepository != null) {
            if (usingRemoteStorage) {
                new Thread(() -> {
                    habitRepository.save(selectedHabit);
                    runOnUiThread(() -> {
                        updateEvaluationDisplay(selectedHabit);
                        Toast.makeText(this, "Habit completed!", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            } else {
                habitRepository.save(selectedHabit);
                updateEvaluationDisplay(selectedHabit);
                Toast.makeText(this, "Habit completed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateEvaluationDisplay(selectedHabit);
            Toast.makeText(this, "Habit completed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHabitClick(Habit habit) {
        selectedHabit = habit;
        updateEvaluationDisplay(habit);
    }

    private void updateEvaluationDisplay(Habit habit) {
        binding.evaluationCard.setVisibility(View.VISIBLE);
        binding.selectedHabitName.setText(habit.getName());

        Evaluation evaluation = evaluatorService.evaluate(
                habit,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        binding.streakText.setText(String.format("Current Streak: %d days", evaluation.getCurrentStreak()));
        binding.completionRateText.setText(String.format("Completion Rate: %.1f%%", evaluation.getCompletionRate() * 100));
        binding.completionProgress.setProgress((int) (evaluation.getCompletionRate() * 100));

        binding.dailyPointsText.setText(getString(R.string.daily_points, scoringService.getCurrentDayScore(habit)));
        binding.weeklyPointsText.setText(getString(R.string.weekly_points, scoringService.getCurrentWeekScore(habit)));
        binding.monthlyPointsText.setText(getString(R.string.monthly_points, scoringService.getCurrentMonthScore(habit)));
    }
}
