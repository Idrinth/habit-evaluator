package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.FragmentHomeBinding;
import de.idrinth.habitevaluator.android.ui.HabitAdapter;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;

public class HomeFragment extends Fragment implements HabitAdapter.OnHabitClickListener {

    private FragmentHomeBinding binding;
    private HabitAdapter habitAdapter;
    private List<Habit> filteredHabits;
    private HabitEvaluatorService evaluatorService;
    private HabitScoringService scoringService;
    private Habit selectedHabit;
    private final Map<String, String> categoryDisplayNameToId = new LinkedHashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filteredHabits = new ArrayList<>();
        evaluatorService = new HabitEvaluatorService();
        scoringService = new HabitScoringService();

        setupRecyclerView();
        setupClickListeners();
        setupCategoryFilterSpinner();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisplayLanguage();
        populateCategorySpinners();
        applyFilter();
        updateLoadDefaultsButtonVisibility();
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

    private void updateDisplayLanguage() {
        if (habitAdapter != null) {
            habitAdapter.setDisplayLanguage(getDisplayLanguage());
        }
    }

    private void setupRecyclerView() {
        habitAdapter = new HabitAdapter(filteredHabits, this);
        habitAdapter.setDisplayLanguage(getDisplayLanguage());
        habitAdapter.setOnHabitEditListener(habit -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEditHabit(habit.getId());
            }
        });
        binding.habitsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.habitsRecyclerView.setAdapter(habitAdapter);
    }

    private void setupClickListeners() {
        binding.completeButton.setOnClickListener(v -> completeHabit());
        binding.removeCompletionButton.setOnClickListener(v -> removeCompletion());
        binding.loadDefaultsButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadDefaults();
            }
        });
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

    private void populateCategorySpinners() {
        categoryDisplayNameToId.clear();
        List<HabitCategory> categoryList = MainActivity.getSharedCategories();
        String displayLanguage = getDisplayLanguage();

        List<String> filterNames = new ArrayList<>();
        filterNames.add(getString(R.string.your_habits));
        for (HabitCategory cat : categoryList) {
            String displayName = displayLanguage != null ? cat.getDisplayName(displayLanguage) : cat.getName();
            String headerName = getString(R.string.your_category_habits, displayName);
            filterNames.add(headerName);
            categoryDisplayNameToId.put(headerName, cat.getId());
        }
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_header_item, filterNames);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categoryFilterSpinner.setAdapter(filterAdapter);
    }

    private void applyFilter() {
        filteredHabits.clear();
        String selected = (String) binding.categoryFilterSpinner.getSelectedItem();
        String yourHabits = getString(R.string.your_habits);
        List<Habit> habits = MainActivity.getSharedHabits();

        if (habits == null) {
            habitAdapter.notifyDataSetChanged();
            return;
        }

        if (selected == null || yourHabits.equals(selected)) {
            filteredHabits.addAll(habits);
        } else {
            String categoryId = categoryDisplayNameToId.get(selected);
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

    private void updateLoadDefaultsButtonVisibility() {
        List<HabitCategory> categoryList = MainActivity.getSharedCategories();
        List<Habit> habits = MainActivity.getSharedHabits();
        if ((categoryList != null && !categoryList.isEmpty()) || (habits != null && !habits.isEmpty())) {
            binding.loadDefaultsButton.setVisibility(View.GONE);
        } else {
            binding.loadDefaultsButton.setVisibility(View.VISIBLE);
        }
    }

    private void completeHabit() {
        if (selectedHabit == null) {
            Toast.makeText(requireContext(), "Please select a habit first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHabit.hasReachedDailyLimit(LocalDate.now())) {
            Toast.makeText(requireContext(), "Daily limit reached for this habit", Toast.LENGTH_SHORT).show();
            return;
        }

        HabitEntry entry = new HabitEntry(selectedHabit.getId());
        selectedHabit.addEntry(entry);

        HabitRepository habitRepository = MainActivity.getSharedHabitRepository();
        boolean usingRemote = MainActivity.isSharedUsingRemoteStorage();

        if (habitRepository != null) {
            if (usingRemote) {
                new Thread(() -> {
                    habitRepository.save(selectedHabit);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            updateEvaluationDisplay(selectedHabit);
                            Toast.makeText(requireContext(), "Habit completed!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                habitRepository.save(selectedHabit);
                updateEvaluationDisplay(selectedHabit);
                Toast.makeText(requireContext(), "Habit completed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateEvaluationDisplay(selectedHabit);
            Toast.makeText(requireContext(), "Habit completed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCompletion() {
        if (selectedHabit == null) {
            Toast.makeText(requireContext(), "Please select a habit first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedHabit.removeLastEntryForDate(LocalDate.now())) {
            Toast.makeText(requireContext(), "No completion to remove for today", Toast.LENGTH_SHORT).show();
            return;
        }

        HabitRepository habitRepository = MainActivity.getSharedHabitRepository();
        boolean usingRemote = MainActivity.isSharedUsingRemoteStorage();

        if (habitRepository != null) {
            if (usingRemote) {
                new Thread(() -> {
                    habitRepository.save(selectedHabit);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            updateEvaluationDisplay(selectedHabit);
                            Toast.makeText(requireContext(), "Completion removed", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                habitRepository.save(selectedHabit);
                updateEvaluationDisplay(selectedHabit);
                Toast.makeText(requireContext(), "Completion removed", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateEvaluationDisplay(selectedHabit);
            Toast.makeText(requireContext(), "Completion removed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHabitClick(Habit habit) {
        selectedHabit = habit;
        updateEvaluationDisplay(habit);
    }

    private void updateEvaluationDisplay(Habit habit) {
        binding.evaluationCard.setVisibility(View.VISIBLE);
        String displayLanguage = getDisplayLanguage();
        if (displayLanguage != null) {
            binding.selectedHabitName.setText(habit.getDisplayName(displayLanguage));
        } else {
            binding.selectedHabitName.setText(habit.getName());
        }

        Evaluation evaluation = evaluatorService.evaluate(
                habit,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        binding.streakText.setText(String.format("Streak: %d days", evaluation.getCurrentStreak()));
        binding.completionRateText.setText(String.format("Rate: %.1f%%", evaluation.getCompletionRate() * 100));
        binding.completedActivitiesText.setText(getString(R.string.completed_activities, evaluation.getTotalEntries()));
        binding.completionProgress.setProgress((int) (evaluation.getCompletionRate() * 100));

        binding.dailyPointsText.setText(getString(R.string.daily_points, scoringService.getCurrentDayScore(habit)));
        binding.weeklyPointsText.setText(getString(R.string.weekly_points, scoringService.getCurrentWeekScore(habit)));
        binding.monthlyPointsText.setText(getString(R.string.monthly_points, scoringService.getCurrentMonthScore(habit)));

        if (habit.getMaxEntriesPerDay() != 1) {
            binding.completeButton.setText(R.string.add_completion);
        } else {
            binding.completeButton.setText(R.string.mark_complete);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
