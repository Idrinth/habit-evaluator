package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.FragmentEditHabitsBinding;
import de.idrinth.habitevaluator.android.ui.EditHabitAdapter;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.ScoringRule;

public class EditHabitsFragment extends Fragment {

    private FragmentEditHabitsBinding binding;
    private EditHabitAdapter adapter;
    private List<Habit> habits;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.saveButton.setOnClickListener(v -> saveEditedHabits());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHabitsList();
    }

    private void refreshHabitsList() {
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
            binding.editRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.editRecyclerView.setAdapter(adapter);
        }

        binding.editMessage.setVisibility(View.GONE);
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

        MainActivity.saveAllHabits();

        String message = getString(R.string.habits_saved_success, count);
        binding.editMessage.setText(message);
        binding.editMessage.setTextColor(requireContext().getResources().getColor(android.R.color.holo_green_dark, requireContext().getTheme()));
        binding.editMessage.setVisibility(View.VISIBLE);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
