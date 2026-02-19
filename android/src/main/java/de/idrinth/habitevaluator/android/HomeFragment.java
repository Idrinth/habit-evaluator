package de.idrinth.habitevaluator.android;

import androidx.appcompat.app.AlertDialog;
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
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
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
        habitAdapter.setOnHabitDeleteListener(this::confirmDeleteHabit);
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
        binding.viewPointsButton.setOnClickListener(v -> {
            if (selectedHabit != null && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToPointDevelopment(selectedHabit.getId());
            }
        });
        binding.addHabitButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddHabit();
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
        filteredHabits.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
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
                            habitAdapter.notifyDataSetChanged();
                            updateEvaluationDisplay(selectedHabit);
                            Toast.makeText(requireContext(), "Habit completed!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                habitRepository.save(selectedHabit);
                habitAdapter.notifyDataSetChanged();
                updateEvaluationDisplay(selectedHabit);
                Toast.makeText(requireContext(), "Habit completed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            habitAdapter.notifyDataSetChanged();
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
                            habitAdapter.notifyDataSetChanged();
                            updateEvaluationDisplay(selectedHabit);
                            Toast.makeText(requireContext(), "Completion removed", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                habitRepository.save(selectedHabit);
                habitAdapter.notifyDataSetChanged();
                updateEvaluationDisplay(selectedHabit);
                Toast.makeText(requireContext(), "Completion removed", Toast.LENGTH_SHORT).show();
            }
        } else {
            habitAdapter.notifyDataSetChanged();
            updateEvaluationDisplay(selectedHabit);
            Toast.makeText(requireContext(), "Completion removed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHabitClick(Habit habit) {
        selectedHabit = habit;
        updateEvaluationDisplay(habit);
    }

    @Override
    public void onHabitDeselect() {
        selectedHabit = null;
        binding.evaluationCard.setVisibility(View.GONE);
    }

    private void updateEvaluationDisplay(Habit habit) {
        binding.evaluationCard.setVisibility(View.VISIBLE);
        String displayLanguage = getDisplayLanguage();
        if (displayLanguage != null) {
            binding.selectedHabitName.setText(habit.getDisplayName(displayLanguage));
        } else {
            binding.selectedHabitName.setText(habit.getName());
        }

        String categoryId = habit.getCategoryId();
        if (categoryId != null && !categoryId.isEmpty()) {
            List<HabitCategory> categories = MainActivity.getSharedCategories();
            String categoryName = null;
            if (categories != null) {
                for (HabitCategory cat : categories) {
                    if (categoryId.equals(cat.getId())) {
                        categoryName = displayLanguage != null ? cat.getDisplayName(displayLanguage) : cat.getName();
                        break;
                    }
                }
            }
            if (categoryName != null) {
                binding.selectedHabitCategory.setText(getString(R.string.habit_category_label, categoryName));
                binding.selectedHabitCategory.setVisibility(View.VISIBLE);
            } else {
                binding.selectedHabitCategory.setVisibility(View.GONE);
            }
        } else {
            binding.selectedHabitCategory.setVisibility(View.GONE);
        }

        Evaluation evaluation = evaluatorService.evaluate(
                habit,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        if (habit.isPositiveScoring()) {
            binding.streakText.setText(String.format("Streak: %d days", evaluation.getCurrentStreak()));
            binding.completionRateText.setText(String.format("Rate: %.1f%%", evaluation.getCompletionRate() * 100));
            binding.completedActivitiesText.setText(getString(R.string.completed_activities, evaluation.getTotalEntries()));
        } else {
            binding.streakText.setText(String.format("Avoided: %d days", evaluation.getCurrentStreak()));
            binding.completionRateText.setText(String.format("Avoidance: %.1f%%", evaluation.getCompletionRate() * 100));
            binding.completedActivitiesText.setText(getString(R.string.occurrences, evaluation.getTotalEntries()));
        }
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

    private void confirmDeleteHabit(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_habit)
                .setMessage(getString(R.string.delete_habit_confirmation, habit.getName()))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteHabit(habit))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteHabit(Habit habit) {
        HabitRepository habitRepository = MainActivity.getSharedHabitRepository();
        boolean usingRemote = MainActivity.isSharedUsingRemoteStorage();
        List<Habit> allHabits = MainActivity.getSharedHabits();

        if (allHabits != null) {
            allHabits.remove(habit);
        }

        if (selectedHabit != null && selectedHabit.getId().equals(habit.getId())) {
            selectedHabit = null;
            binding.evaluationCard.setVisibility(View.GONE);
        }

        String categoryId = habit.getCategoryId();
        HabitCategoryRepository categoryRepository = MainActivity.getSharedCategoryRepository();

        if (habitRepository != null) {
            if (usingRemote) {
                new Thread(() -> {
                    habitRepository.deleteById(habit.getId());
                    cleanupOrphanedCategory(categoryId, allHabits, categoryRepository);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            applyFilter();
                            Toast.makeText(requireContext(), R.string.habit_deleted, Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                habitRepository.deleteById(habit.getId());
                cleanupOrphanedCategory(categoryId, allHabits, categoryRepository);
                applyFilter();
                Toast.makeText(requireContext(), R.string.habit_deleted, Toast.LENGTH_SHORT).show();
            }
        } else {
            cleanupOrphanedCategory(categoryId, allHabits, categoryRepository);
            applyFilter();
            Toast.makeText(requireContext(), R.string.habit_deleted, Toast.LENGTH_SHORT).show();
        }
    }

    static boolean isCategoryUsed(String categoryId, List<Habit> habits) {
        if (categoryId == null || categoryId.isEmpty()) {
            return false;
        }
        return habits != null && habits.stream()
                .anyMatch(h -> categoryId.equals(h.getCategoryId()));
    }

    private void cleanupOrphanedCategory(String categoryId, List<Habit> allHabits, HabitCategoryRepository categoryRepository) {
        if (categoryId == null || categoryId.isEmpty() || categoryRepository == null) {
            return;
        }
        if (!isCategoryUsed(categoryId, allHabits)) {
            categoryRepository.deleteById(categoryId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
