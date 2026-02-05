package de.idrinth.habitevaluator.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.FragmentAddHabitBinding;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;

public class AddHabitFragment extends Fragment {

    private FragmentAddHabitBinding binding;
    private List<HabitCategory> categoryList = new ArrayList<>();
    private final Map<String, String> categoryDisplayNameToId = new LinkedHashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFrequencyTypeSpinner();
        binding.addHabitButton.setOnClickListener(v -> addHabit());
    }

    @Override
    public void onResume() {
        super.onResume();
        categoryList = new ArrayList<>(MainActivity.getSharedCategories());
        populateCategorySpinner();
    }

    private void setupFrequencyTypeSpinner() {
        List<String> frequencyTypes = new ArrayList<>();
        for (FrequencyType ft : FrequencyType.values()) {
            frequencyTypes.add(getFrequencyTypeLabel(ft));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, frequencyTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.frequencyTypeSpinner.setAdapter(adapter);
    }

    private String getFrequencyTypeLabel(FrequencyType ft) {
        switch (ft) {
            case DAILY:
                return getString(R.string.frequency_daily);
            case WEEKLY:
                return getString(R.string.frequency_weekly);
            case MONTHLY:
                return getString(R.string.frequency_monthly);
            default:
                return ft.name().substring(0, 1) + ft.name().substring(1).toLowerCase();
        }
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

    private void populateCategorySpinner() {
        categoryDisplayNameToId.clear();
        String displayLanguage = getDisplayLanguage();
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.new_category));
        for (HabitCategory cat : categoryList) {
            String displayName = displayLanguage != null ? cat.getDisplayName(displayLanguage) : cat.getName();
            categoryNames.add(displayName);
            categoryDisplayNameToId.put(displayName, cat.getId());
        }
        ArrayAdapter<String> createAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        createAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(createAdapter);
        if (!categoryList.isEmpty()) {
            binding.categorySpinner.setSelection(1);
        }
        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (getString(R.string.new_category).equals(selected)) {
                    showNewCategoryDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showNewCategoryDialog() {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.new_category_hint);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_category_dialog_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (categoryName.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.category_name_required, Toast.LENGTH_SHORT).show();
                        binding.categorySpinner.setSelection(0);
                        return;
                    }
                    createCategory(categoryName);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (categoryList.isEmpty()) {
                        binding.categorySpinner.setSelection(0);
                    } else {
                        binding.categorySpinner.setSelection(1);
                    }
                })
                .show();
    }

    private void createCategory(String name) {
        HabitCategory category = new HabitCategory(name);
        User currentUser = MainActivity.getSharedCurrentUser();
        category.setUser(currentUser);
        boolean usingRemote = MainActivity.isSharedUsingRemoteStorage();
        ApiClient apiClient = MainActivity.getSharedApiClient();
        HabitCategoryRepository categoryRepository = MainActivity.getSharedCategoryRepository();

        if (usingRemote && apiClient != null) {
            new Thread(() -> {
                try {
                    Map<String, String> body = new LinkedHashMap<>();
                    body.put("name", name);
                    HabitCategory created = apiClient.post("/api/categories", body, HabitCategory.class);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            categoryList.add(created);
                            MainActivity.getSharedCategories().add(created);
                            populateCategorySpinner();
                            int position = findCategorySpinnerPosition(created.getName());
                            binding.categorySpinner.setSelection(position);
                        });
                    }
                } catch (IOException e) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to create category: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
                    }
                }
            }).start();
        } else if (categoryRepository != null) {
            categoryRepository.save(category);
            categoryList.add(category);
            MainActivity.getSharedCategories().add(category);
            populateCategorySpinner();
            int position = findCategorySpinnerPosition(category.getName());
            binding.categorySpinner.setSelection(position);
        }
    }

    private int findCategorySpinnerPosition(String categoryName) {
        for (int i = 0; i < binding.categorySpinner.getAdapter().getCount(); i++) {
            if (categoryName.equals(binding.categorySpinner.getAdapter().getItem(i))) {
                return i;
            }
        }
        return 0;
    }

    private void addHabit() {
        String name = binding.habitNameInput.getText().toString().trim();
        String description = binding.habitDescriptionInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = MainActivity.getSharedCurrentUser();
        Habit habit = new Habit(name, description);
        habit.setUser(currentUser);

        String selectedCategory = (String) binding.categorySpinner.getSelectedItem();
        if (selectedCategory == null || getString(R.string.new_category).equals(selectedCategory)) {
            Toast.makeText(requireContext(), R.string.category_required, Toast.LENGTH_SHORT).show();
            return;
        }
        String catId = categoryDisplayNameToId.get(selectedCategory);
        if (catId != null) {
            habit.setCategoryId(catId);
        }

        int freqPos = binding.frequencyTypeSpinner.getSelectedItemPosition();
        if (freqPos >= 0 && freqPos < FrequencyType.values().length) {
            habit.setFrequencyType(FrequencyType.values()[freqPos]);
        }

        try {
            int targetFreq = Integer.parseInt(binding.targetFrequencyInput.getText().toString().trim());
            if (targetFreq > 0) {
                habit.setTargetFrequency(targetFreq);
            }
        } catch (NumberFormatException e) {
            // keep default
        }

        try {
            int maxEntries = Integer.parseInt(binding.maxEntriesPerDayInput.getText().toString().trim());
            if (maxEntries > 0) {
                habit.setMaxEntriesPerDay(maxEntries);
            }
        } catch (NumberFormatException e) {
            // keep default
        }

        habit.setPositiveScoring(binding.positiveScoringSwitch.isChecked());

        try {
            int t1 = Integer.parseInt(binding.threshold1Input.getText().toString().trim());
            int t2 = Integer.parseInt(binding.threshold2Input.getText().toString().trim());
            int t4 = Integer.parseInt(binding.threshold4Input.getText().toString().trim());
            int t8 = Integer.parseInt(binding.threshold8Input.getText().toString().trim());
            if (t1 >= 0 && t2 >= t1 && t4 >= t2 && t8 >= t4) {
                ScoringRule rule = new ScoringRule("custom", t1, t2, t4, t8);
                habit.setScoringRule(rule);
            }
        } catch (NumberFormatException e) {
            // keep default scoring rule
        }

        HabitRepository habitRepository = MainActivity.getSharedHabitRepository();
        boolean usingRemote = MainActivity.isSharedUsingRemoteStorage();
        List<Habit> habits = MainActivity.getSharedHabits();

        if (habitRepository != null) {
            if (usingRemote) {
                new Thread(() -> {
                    habitRepository.save(habit);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            if (habits != null) {
                                habits.add(habit);
                            }
                            Toast.makeText(requireContext(), "Habit added", Toast.LENGTH_SHORT).show();
                            clearForm();
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).navigateToHome();
                            }
                        });
                    }
                }).start();
            } else {
                habitRepository.save(habit);
                if (habits != null) {
                    habits.add(habit);
                }
                Toast.makeText(requireContext(), "Habit added", Toast.LENGTH_SHORT).show();
                clearForm();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToHome();
                }
            }
        } else {
            if (habits != null) {
                habits.add(habit);
            }
            Toast.makeText(requireContext(), "Habit added", Toast.LENGTH_SHORT).show();
            clearForm();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHome();
            }
        }
    }

    private void clearForm() {
        binding.habitNameInput.setText("");
        binding.habitDescriptionInput.setText("");
        binding.targetFrequencyInput.setText("1");
        binding.maxEntriesPerDayInput.setText("1");
        binding.positiveScoringSwitch.setChecked(true);
        binding.threshold1Input.setText("1");
        binding.threshold2Input.setText("2");
        binding.threshold4Input.setText("4");
        binding.threshold8Input.setText("7");
        if (binding.frequencyTypeSpinner.getAdapter() != null && binding.frequencyTypeSpinner.getAdapter().getCount() > 0) {
            binding.frequencyTypeSpinner.setSelection(0);
        }
        if (!categoryList.isEmpty()) {
            binding.categorySpinner.setSelection(1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
