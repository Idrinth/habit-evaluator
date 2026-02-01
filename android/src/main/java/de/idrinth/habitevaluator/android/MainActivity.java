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
import de.idrinth.habitevaluator.shared.model.FrequencyType;
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

    public static List<Habit> getSharedHabits() {
        return sharedHabits;
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
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = new ArrayList<>();
        filteredHabits = new ArrayList<>();
        sharedHabits = habits;
        evaluatorService = new HabitEvaluatorService();
        scoringService = new HabitScoringService();

        setupRecyclerView();
        setupFrequencyTypeSpinner();
        initializeStorage();
        setupClickListeners();
        setupCategoryFilterSpinner();
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
        currentUser = new User(PLACEHOLDER_USERNAME, "placeholder");
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
                        populateCategorySpinners();
                        loadHabits();
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        populateCategorySpinners();
                        loadHabits();
                    });
                }
            }).start();
        } else if (categoryRepository != null && currentUser != null) {
            categoryList.addAll(categoryRepository.findByUserId(currentUser.getId()));
            populateCategorySpinners();
            loadHabits();
        } else {
            populateCategorySpinners();
            loadHabits();
        }
    }

    private void populateCategorySpinners() {
        categoryNameToId.clear();

        // Category creation spinner
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.no_category));
        for (HabitCategory cat : categoryList) {
            categoryNames.add(cat.getName());
            categoryNameToId.put(cat.getName(), cat.getId());
        }
        ArrayAdapter<String> createAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        createAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(createAdapter);

        // Category filter spinner
        List<String> filterNames = new ArrayList<>();
        filterNames.add(getString(R.string.all_categories));
        for (HabitCategory cat : categoryList) {
            filterNames.add(cat.getName());
        }
        filterNames.add(getString(R.string.uncategorized));
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterNames);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categoryFilterSpinner.setAdapter(filterAdapter);
    }

    private void setupFrequencyTypeSpinner() {
        List<String> frequencyTypes = new ArrayList<>();
        for (FrequencyType ft : FrequencyType.values()) {
            frequencyTypes.add(ft.name().substring(0, 1) + ft.name().substring(1).toLowerCase());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, frequencyTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.frequencyTypeSpinner.setAdapter(adapter);
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
        String uncategorized = getString(R.string.uncategorized);

        if (selected == null || allCategories.equals(selected)) {
            filteredHabits.addAll(habits);
        } else if (uncategorized.equals(selected)) {
            for (Habit h : habits) {
                if (h.getCategoryId() == null || h.getCategoryId().isEmpty()) {
                    filteredHabits.add(h);
                }
            }
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
                    });
                }).start();
            } else {
                habits.addAll(habitRepository.findByUserId(currentUser.getId()));
            }
        }
        applyFilter();
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
        binding.addHabitButton.setOnClickListener(v -> addHabit());
        binding.completeButton.setOnClickListener(v -> completeHabit());
        binding.trackHabitsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrackHabitsActivity.class);
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

    private void addHabit() {
        String name = binding.habitNameInput.getText().toString().trim();
        String description = binding.habitDescriptionInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show();
            return;
        }

        Habit habit = new Habit(name, description);
        habit.setUser(currentUser);

        String selectedCategory = (String) binding.categorySpinner.getSelectedItem();
        if (selectedCategory != null && !getString(R.string.no_category).equals(selectedCategory)) {
            String catId = categoryNameToId.get(selectedCategory);
            if (catId != null) {
                habit.setCategoryId(catId);
            }
        }

        // Set frequency type
        int freqPos = binding.frequencyTypeSpinner.getSelectedItemPosition();
        if (freqPos >= 0 && freqPos < FrequencyType.values().length) {
            habit.setFrequencyType(FrequencyType.values()[freqPos]);
        }

        // Set target frequency
        try {
            int targetFreq = Integer.parseInt(binding.targetFrequencyInput.getText().toString().trim());
            if (targetFreq > 0) {
                habit.setTargetFrequency(targetFreq);
            }
        } catch (NumberFormatException e) {
            // keep default
        }

        // Set max entries per day
        try {
            int maxEntries = Integer.parseInt(binding.maxEntriesPerDayInput.getText().toString().trim());
            if (maxEntries > 0) {
                habit.setMaxEntriesPerDay(maxEntries);
            }
        } catch (NumberFormatException e) {
            // keep default
        }

        // Set positive/negative scoring
        habit.setPositiveScoring(binding.positiveScoringSwitch.isChecked());

        if (habitRepository != null) {
            if (usingRemoteStorage) {
                new Thread(() -> {
                    habitRepository.save(habit);
                    runOnUiThread(() -> {
                        habits.add(habit);
                        applyFilter();
                    });
                }).start();
            } else {
                habitRepository.save(habit);
                habits.add(habit);
                applyFilter();
            }
        } else {
            habits.add(habit);
            applyFilter();
        }

        binding.habitNameInput.setText("");
        binding.habitDescriptionInput.setText("");
        binding.categorySpinner.setSelection(0);
        binding.frequencyTypeSpinner.setSelection(0);
        binding.targetFrequencyInput.setText("1");
        binding.maxEntriesPerDayInput.setText("1");
        binding.positiveScoringSwitch.setChecked(true);
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
