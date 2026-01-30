package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.idrinth.habitevaluator.android.databinding.ActivityTrackHabitsBinding;
import de.idrinth.habitevaluator.android.ui.TrackHabitAdapter;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;

public class TrackHabitsActivity extends AppCompatActivity {

    public static final String EXTRA_HABITS = "habits";

    private ActivityTrackHabitsBinding binding;
    private TrackHabitAdapter adapter;
    private List<Habit> habits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackHabitsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = getIntent().getParcelableArrayListExtra(EXTRA_HABITS) != null
                ? new ArrayList<>() : new ArrayList<>();

        // Habits are stored in the application-level list via MainActivity
        habits = MainActivity.getSharedHabits();

        if (habits == null || habits.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.trackRecyclerView.setVisibility(View.GONE);
            binding.submitButton.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.trackRecyclerView.setVisibility(View.VISIBLE);
            binding.submitButton.setVisibility(View.VISIBLE);
            adapter = new TrackHabitAdapter(habits);
            binding.trackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.trackRecyclerView.setAdapter(adapter);
        }

        binding.submitButton.setOnClickListener(v -> submitTrackedHabits());
    }

    private void submitTrackedHabits() {
        if (adapter == null) {
            return;
        }
        Set<String> checkedIds = adapter.getCheckedHabitIds();
        if (checkedIds.isEmpty()) {
            binding.trackMessage.setText(R.string.no_habits_selected);
            binding.trackMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
            binding.trackMessage.setVisibility(View.VISIBLE);
            return;
        }

        int count = 0;
        for (Habit habit : habits) {
            if (checkedIds.contains(habit.getId())) {
                HabitEntry entry = new HabitEntry(habit.getId());
                habit.addEntry(entry);
                count++;
            }
        }

        binding.trackMessage.setText(getString(R.string.habits_tracked_success, count));
        binding.trackMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        binding.trackMessage.setVisibility(View.VISIBLE);
        Toast.makeText(this, getString(R.string.habits_tracked_success, count), Toast.LENGTH_SHORT).show();

        // Reset checkboxes
        adapter.getCheckedHabitIds().clear();
        adapter.notifyDataSetChanged();
    }
}
