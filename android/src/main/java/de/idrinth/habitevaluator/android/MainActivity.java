package de.idrinth.habitevaluator.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.ActivityMainBinding;
import de.idrinth.habitevaluator.android.ui.HabitAdapter;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = new ArrayList<>();
        sharedHabits = habits;
        evaluatorService = new HabitEvaluatorService();

        // Initialize placeholder user for Android
        currentUser = new User(PLACEHOLDER_USERNAME, "placeholder");

        setupRecyclerView();
        setupClickListeners();
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
        habits.add(habit);
        habitAdapter.notifyItemInserted(habits.size() - 1);

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
        updateEvaluationDisplay(selectedHabit);
        Toast.makeText(this, "Habit completed!", Toast.LENGTH_SHORT).show();
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
