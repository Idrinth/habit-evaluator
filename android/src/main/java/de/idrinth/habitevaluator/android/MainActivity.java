package de.idrinth.habitevaluator.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.ActivityMainBinding;
import de.idrinth.habitevaluator.android.ui.HabitAdapter;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;

public class MainActivity extends AppCompatActivity implements HabitAdapter.OnHabitClickListener {

    private static final String PLACEHOLDER_USERNAME = "android_user";
    private static List<Habit> sharedHabits;

    public static List<Habit> getSharedHabits() {
        return sharedHabits;
    }

    private ActivityMainBinding binding;
    private HabitAdapter habitAdapter;
    private List<Habit> habits;
    private HabitEvaluatorService evaluatorService;
    private Habit selectedHabit;
    private User currentUser;
    private HabitRepository habitRepository;
    private boolean usingRemoteStorage;

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
        sharedHabits = habits;
        evaluatorService = new HabitEvaluatorService();

        initializeStorage();
        setupRecyclerView();
        setupClickListeners();
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
        loadHabits();
    }

    private void initializeLocalStorage() {
        usingRemoteStorage = false;
        habitRepository = null;
        currentUser = new User(PLACEHOLDER_USERNAME, "placeholder");
    }

    private void initializeRemoteStorage(SharedPreferences prefs) {
        String url = prefs.getString(SettingsActivity.KEY_API_URL, "");
        String username = prefs.getString(SettingsActivity.KEY_API_USERNAME, "");
        String password = prefs.getString(SettingsActivity.KEY_API_PASSWORD, "");

        new Thread(() -> {
            try {
                ApiClient apiClient = new ApiClient(url);
                boolean loggedIn = apiClient.login(username, password);
                if (loggedIn) {
                    habitRepository = new RemoteHabitRepository(apiClient);
                    RemoteUserRepository userRepo = new RemoteUserRepository(apiClient);
                    currentUser = userRepo.findAll().stream().findFirst().orElse(null);
                    usingRemoteStorage = true;
                    runOnUiThread(() -> {
                        updateStorageModeLabel();
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
                loadHabits();
            });
        }).start();
    }

    private void loadHabits() {
        habits.clear();
        if (usingRemoteStorage && habitRepository != null && currentUser != null) {
            new Thread(() -> {
                List<Habit> remoteHabits = habitRepository.findByUserId(currentUser.getId());
                runOnUiThread(() -> {
                    habits.addAll(remoteHabits);
                    habitAdapter.notifyDataSetChanged();
                });
            }).start();
        }
        habitAdapter.notifyDataSetChanged();
    }

    private void updateStorageModeLabel() {
        if (usingRemoteStorage) {
            binding.storageModeText.setText(R.string.storage_mode_remote);
        } else {
            binding.storageModeText.setText(R.string.storage_mode_local);
        }
    }

    private void setupRecyclerView() {
        habitAdapter = new HabitAdapter(habits, this);
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

        if (usingRemoteStorage && habitRepository != null) {
            new Thread(() -> {
                habitRepository.save(habit);
                runOnUiThread(() -> {
                    habits.add(habit);
                    habitAdapter.notifyItemInserted(habits.size() - 1);
                });
            }).start();
        } else {
            habits.add(habit);
            habitAdapter.notifyItemInserted(habits.size() - 1);
        }

        binding.habitNameInput.setText("");
        binding.habitDescriptionInput.setText("");
    }

    private void completeHabit() {
        if (selectedHabit == null) {
            Toast.makeText(this, "Please select a habit first", Toast.LENGTH_SHORT).show();
            return;
        }

        HabitEntry entry = new HabitEntry(selectedHabit.getId());
        selectedHabit.addEntry(entry);

        if (usingRemoteStorage && habitRepository != null) {
            new Thread(() -> {
                habitRepository.save(selectedHabit);
                runOnUiThread(() -> {
                    updateEvaluationDisplay(selectedHabit);
                    Toast.makeText(this, "Habit completed!", Toast.LENGTH_SHORT).show();
                });
            }).start();
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
    }
}
