package de.idrinth.habitevaluator.android;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import de.idrinth.habitevaluator.android.databinding.FragmentFoodLogBinding;
import de.idrinth.habitevaluator.android.ui.FoodLogAdapter;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;

public class FoodLogFragment extends Fragment implements FoodLogAdapter.OnFoodLogDeleteListener {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private FragmentFoodLogBinding binding;
    private FoodLogAdapter adapter;
    private List<FoodLog> displayedEntries;
    private LocalDate selectedDate;
    private LocalTime selectedTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFoodLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayedEntries = new ArrayList<>();
        selectedDate = LocalDate.now();
        selectedTime = LocalTime.now().withSecond(0).withNano(0);

        setupRecyclerView();
        setupDateTimePicker();
        binding.addFoodLogButton.setOnClickListener(v -> addFoodLog());
        loadEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEntries();
    }

    private void setupRecyclerView() {
        adapter = new FoodLogAdapter(displayedEntries, this);
        binding.foodLogRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.foodLogRecyclerView.setAdapter(adapter);
    }

    private void setupDateTimePicker() {
        updateDateTimeDisplay();
        binding.foodDateTimeInput.setOnClickListener(v -> showDatePicker());
    }

    private void updateDateTimeDisplay() {
        LocalDateTime dt = LocalDateTime.of(selectedDate, selectedTime);
        binding.foodDateTimeInput.setText(dt.format(DT_DISPLAY_FORMAT));
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    showTimePicker();
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker() {
        int hour = selectedTime != null ? selectedTime.getHour() : LocalTime.now().getHour();
        int minute = selectedTime != null ? selectedTime.getMinute() : 0;

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = LocalTime.of(selectedHour, selectedMinute);
                    updateDateTimeDisplay();
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void addFoodLog() {
        String foodItems = binding.foodItemsInput.getText() != null
                ? binding.foodItemsInput.getText().toString().trim() : "";
        String kcalStr = binding.kcalInput.getText() != null
                ? binding.kcalInput.getText().toString().trim() : "";
        String carbsStr = binding.carbsInput.getText() != null
                ? binding.carbsInput.getText().toString().trim() : "";

        if (foodItems.isEmpty()) {
            Toast.makeText(requireContext(), R.string.food_log_items_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer kcal = null;
        if (!kcalStr.isEmpty()) {
            try {
                kcal = Integer.parseInt(kcalStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.food_log_kcal_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Double carbs = null;
        if (!carbsStr.isEmpty()) {
            try {
                carbs = Double.parseDouble(carbsStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.food_log_carbs_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        LocalDateTime dateTime = LocalDateTime.of(selectedDate, selectedTime);
        FoodLog entry = new FoodLog(carbs, kcal, dateTime, foodItems);
        entry.setUser(MainActivity.getSharedLocalUser());

        String notes = binding.foodNotesInput.getText() != null
                ? binding.foodNotesInput.getText().toString().trim() : "";
        if (!notes.isEmpty()) {
            entry.setNotes(notes);
        }

        FoodLogRepository repository = MainActivity.getSharedFoodLogRepository();
        if (repository != null) {
            repository.save(entry);
        }

        binding.foodItemsInput.setText("");
        binding.kcalInput.setText("");
        binding.carbsInput.setText("");
        binding.foodNotesInput.setText("");
        selectedDate = LocalDate.now();
        selectedTime = LocalTime.now().withSecond(0).withNano(0);
        updateDateTimeDisplay();

        Toast.makeText(requireContext(), R.string.food_log_entry_added, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    @Override
    public void onDelete(FoodLog entry) {
        FoodLogRepository repository = MainActivity.getSharedFoodLogRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }
        Toast.makeText(requireContext(), R.string.food_log_entry_removed, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    private void loadEntries() {
        displayedEntries.clear();

        FoodLogRepository repository = MainActivity.getSharedFoodLogRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<FoodLog> allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
            allEntries.sort(Comparator.comparing(FoodLog::getDateTime).reversed()
                    .thenComparing(Comparator.comparing(FoodLog::getCreatedAt).reversed()));
            displayedEntries.addAll(allEntries);
        }

        adapter.notifyDataSetChanged();
        updateSuggestions();
    }

    private void updateSuggestions() {
        FoodLogRepository repository = MainActivity.getSharedFoodLogRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<FoodLog> allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
            List<String> suggestions = allEntries.stream()
                    .flatMap(e -> e.getFoodItemList().stream())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            ArrayAdapter<String> suggestionsAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            binding.foodItemsInput.setAdapter(suggestionsAdapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
