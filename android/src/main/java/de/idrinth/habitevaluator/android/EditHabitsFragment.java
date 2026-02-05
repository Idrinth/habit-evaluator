package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.FragmentEditHabitsBinding;
import de.idrinth.habitevaluator.android.ui.EditHabitAdapter;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
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

    private boolean isTranslationsEnabled() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SettingsActivity.KEY_CUSTOM_TRANSLATIONS, false);
    }

    private String getDisplayLanguage() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        boolean translationsEnabled = prefs.getBoolean(SettingsActivity.KEY_CUSTOM_TRANSLATIONS, false);
        if (!translationsEnabled) {
            return null;
        }
        String language = prefs.getString(SettingsActivity.KEY_LANGUAGE, SettingsActivity.LANGUAGE_SYSTEM);
        return SettingsActivity.getEffectiveLanguage(language);
    }

    private void refreshHabitsList() {
        String editHabitId = MainActivity.getEditHabitId();
        List<Habit> allHabits = MainActivity.getSharedHabits();
        habits = new ArrayList<>();

        if (editHabitId != null && allHabits != null) {
            for (Habit h : allHabits) {
                if (editHabitId.equals(h.getId())) {
                    habits.add(h);
                    break;
                }
            }
        }

        if (habits.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.editRecyclerView.setVisibility(View.GONE);
            binding.saveButton.setVisibility(View.GONE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            binding.editRecyclerView.setVisibility(View.VISIBLE);
            binding.saveButton.setVisibility(View.VISIBLE);
            List<HabitCategory> categories = MainActivity.getSharedCategories();
            adapter = new EditHabitAdapter(habits, isTranslationsEnabled(),
                    categories != null ? categories : new java.util.ArrayList<>(),
                    getDisplayLanguage());
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
        boolean translationsEnabled = isTranslationsEnabled();
        int count = 0;
        for (Habit habit : habits) {
            EditHabitAdapter.EditedHabitValues values = editedValues.get(habit.getId());
            if (values == null) {
                continue;
            }
            boolean changed = false;

            if (values.categoryId != null && !values.categoryId.equals(habit.getCategoryId())) {
                habit.setCategoryId(values.categoryId);
                changed = true;
            }
            if (values.frequencyType != null && values.frequencyType != habit.getFrequencyType()) {
                habit.setFrequencyType(values.frequencyType);
                changed = true;
            }
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

            if (translationsEnabled) {
                if (!values.nameTranslations.equals(habit.getNameTranslations())) {
                    habit.setNameTranslations(values.nameTranslations);
                    changed = true;
                }
                if (!values.descriptionTranslations.equals(habit.getDescriptionTranslations())) {
                    habit.setDescriptionTranslations(values.descriptionTranslations);
                    changed = true;
                }
            }

            if (changed) {
                count++;
            }
        }

        MainActivity.saveAllHabits();

        String message = getString(R.string.habits_saved_success, count);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToHome();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
