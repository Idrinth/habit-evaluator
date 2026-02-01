package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.ActivityEditHabitsBinding;
import de.idrinth.habitevaluator.android.ui.EditHabitAdapter;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

public class EditHabitsActivity extends AppCompatActivity {

    private ActivityEditHabitsBinding binding;
    private EditHabitAdapter adapter;
    private List<Habit> habits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditHabitsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = MainActivity.getSharedHabits();

        if (habits == null || habits.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.editRecyclerView.setVisibility(View.GONE);
            binding.saveButton.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.editRecyclerView.setVisibility(View.VISIBLE);
            binding.saveButton.setVisibility(View.VISIBLE);
            adapter = new EditHabitAdapter(habits);
            binding.editRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.editRecyclerView.setAdapter(adapter);
        }

        binding.saveButton.setOnClickListener(v -> saveEditedHabits());
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
        binding.editMessage.setText(message);
        binding.editMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        binding.editMessage.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
