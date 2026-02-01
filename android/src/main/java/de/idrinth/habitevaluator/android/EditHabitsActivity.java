package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.ActivityTrackHabitsBinding;
import de.idrinth.habitevaluator.android.ui.EditHabitAdapter;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

public class EditHabitsActivity extends AppCompatActivity {

    private ActivityTrackHabitsBinding binding;
    private EditHabitAdapter adapter;
    private List<Habit> habits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackHabitsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = MainActivity.getSharedHabits();

        binding.trackTitle.setText(R.string.edit_habits);

        if (habits == null || habits.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.trackRecyclerView.setVisibility(View.GONE);
            binding.submitButton.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.trackRecyclerView.setVisibility(View.VISIBLE);
            binding.submitButton.setVisibility(View.VISIBLE);
            binding.submitButton.setText(R.string.save);
            adapter = new EditHabitAdapter(habits);
            binding.trackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.trackRecyclerView.setAdapter(adapter);
        }

        binding.submitButton.setOnClickListener(v -> saveEditedHabits());
    }

    private void saveEditedHabits() {
        if (adapter == null) {
            return;
        }
        Map<String, EditHabitAdapter.EditedHabitValues> editedValues = adapter.getEditedValues();
        int count = 0;
        for (Habit habit : habits) {
            EditHabitAdapter.EditedHabitValues values = editedValues.get(habit.getId());
            if (values == null) {
                continue;
            }
            boolean changed = false;

            if (habit.getTargetFrequency() != values.targetFrequency && values.targetFrequency > 0) {
                habit.setTargetFrequency(values.targetFrequency);
                changed = true;
            }
            if (habit.getMaxEntriesPerDay() != values.maxEntriesPerDay && values.maxEntriesPerDay >= 0) {
                habit.setMaxEntriesPerDay(values.maxEntriesPerDay);
                changed = true;
            }
            if (habit.isPositiveScoring() != values.positiveScoring) {
                habit.setPositiveScoring(values.positiveScoring);
                changed = true;
            }

            ScoringRule rule = habit.getScoringRule();
            if (rule != null) {
                if (values.threshold1 >= 0 && values.threshold2 >= values.threshold1
                        && values.threshold4 >= values.threshold2 && values.threshold8 >= values.threshold4) {
                    if (rule.getThresholdFor1Point() != values.threshold1
                            || rule.getThresholdFor2Points() != values.threshold2
                            || rule.getThresholdFor4Points() != values.threshold4
                            || rule.getThresholdFor8Points() != values.threshold8) {
                        rule = new ScoringRule(rule.getName(), values.threshold1, values.threshold2,
                                values.threshold4, values.threshold8);
                        habit.setScoringRule(rule);
                        changed = true;
                    }
                }
            }

            if (changed) {
                count++;
            }
        }

        // Persist changes via MainActivity's repository
        MainActivity.saveAllHabits();

        String message = getString(R.string.habits_saved_success, count);
        binding.trackMessage.setText(message);
        binding.trackMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        binding.trackMessage.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
