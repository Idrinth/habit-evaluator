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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentSportLogBinding;
import de.idrinth.habitevaluator.android.ui.SportLogAdapter;
import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.SportLogStats;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.service.SportLogService;

public class SportLogFragment extends Fragment implements SportLogAdapter.OnSportLogDeleteListener {

    static final String DATE_PATTERN = "yyyy-MM-dd";
    static final String TIME_PATTERN = "HH:mm";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private FragmentSportLogBinding binding;
    private SportLogAdapter adapter;
    private List<SportLog> displayedEntries;
    private SportLogService sportLogService;
    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSportLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayedEntries = new ArrayList<>();
        sportLogService = new SportLogService();
        selectedDate = LocalDate.now();

        setupRecyclerView();
        setupDatePicker();
        setupTimePickers();
        setupFormToggle();
        binding.addSportLogButton.setOnClickListener(v -> addSportLog());
        loadEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEntries();
    }

    private void setupRecyclerView() {
        adapter = new SportLogAdapter(displayedEntries, this);
        binding.sportLogRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.sportLogRecyclerView.setAdapter(adapter);
    }

    private void setupFormToggle() {
        binding.addEntryHeader.setOnClickListener(v -> toggleForm());
        binding.toggleFormButton.setOnClickListener(v -> toggleForm());
    }

    private void toggleForm() {
        boolean isVisible = binding.addEntryFormContainer.getVisibility() == View.VISIBLE;
        binding.addEntryFormContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.toggleFormButton.setImageResource(isVisible
                ? android.R.drawable.arrow_down_float
                : android.R.drawable.arrow_up_float);
    }

    private void setupDatePicker() {
        binding.sportDateInput.setText(selectedDate.format(DATE_FORMAT));
        binding.sportDateInput.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    binding.sportDateInput.setText(selectedDate.format(DATE_FORMAT));
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupTimePickers() {
        binding.sportStartTimeInput.setOnClickListener(v -> showTimePicker(true));
        binding.sportEndTimeInput.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStartTime) {
        LocalTime current = isStartTime ? selectedStartTime : selectedEndTime;
        int hour = current != null ? current.getHour() : LocalTime.now().getHour();
        int minute = current != null ? current.getMinute() : 0;

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    LocalTime time = LocalTime.of(selectedHour, selectedMinute);
                    if (isStartTime) {
                        selectedStartTime = time;
                        binding.sportStartTimeInput.setText(time.format(TIME_FORMAT));
                    } else {
                        selectedEndTime = time;
                        binding.sportEndTimeInput.setText(time.format(TIME_FORMAT));
                    }
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void addSportLog() {
        String name = binding.activityNameInput.getText() != null
                ? binding.activityNameInput.getText().toString().trim() : "";
        String measurementStr = binding.measurementInput.getText() != null
                ? binding.measurementInput.getText().toString().trim() : "";
        String measurementUnit = binding.measurementUnitInput.getText() != null
                ? binding.measurementUnitInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.sport_log_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (measurementStr.isEmpty() || measurementUnit.isEmpty()) {
            Toast.makeText(requireContext(), R.string.sport_log_measurement_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(requireContext(), R.string.sport_log_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double measurement;
        try {
            measurement = Double.parseDouble(measurementStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.sport_log_measurement_required, Toast.LENGTH_SHORT).show();
            return;
        }

        SportLog entry = new SportLog(name, measurement, measurementUnit,
                selectedStartTime, selectedEndTime, selectedDate);
        entry.setUser(MainActivity.getSharedLocalUser());

        String notes = binding.sportNotesInput.getText() != null
                ? binding.sportNotesInput.getText().toString().trim() : "";
        if (!notes.isEmpty()) {
            entry.setNotes(notes);
        }

        SportLogRepository repository = MainActivity.getSharedSportLogRepository();
        if (repository != null) {
            repository.save(entry);
        }

        binding.activityNameInput.setText("");
        binding.measurementInput.setText("");
        binding.measurementUnitInput.setText("");
        binding.sportNotesInput.setText("");
        selectedStartTime = null;
        selectedEndTime = null;
        binding.sportStartTimeInput.setText("");
        binding.sportEndTimeInput.setText("");
        selectedDate = LocalDate.now();
        binding.sportDateInput.setText(selectedDate.format(DATE_FORMAT));

        binding.addEntryFormContainer.setVisibility(View.GONE);
        binding.toggleFormButton.setImageResource(android.R.drawable.arrow_down_float);

        Toast.makeText(requireContext(), R.string.sport_log_entry_added, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    @Override
    public void onDelete(SportLog entry) {
        SportLogRepository repository = MainActivity.getSharedSportLogRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }
        Toast.makeText(requireContext(), R.string.sport_log_entry_removed, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    private void loadEntries() {
        displayedEntries.clear();

        SportLogRepository repository = MainActivity.getSharedSportLogRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<SportLog> allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
            allEntries.sort(Comparator.comparing(SportLog::getDate).reversed()
                    .thenComparing(Comparator.comparing(SportLog::getCreatedAt).reversed()));
            displayedEntries.addAll(allEntries);
        }

        adapter.notifyDataSetChanged();
        updateSuggestions();
        updateStats();
    }

    private void updateSuggestions() {
        SportLogRepository repository = MainActivity.getSharedSportLogRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            String userId = MainActivity.getSharedLocalUser().getId();
            List<String> names = repository.findDistinctNamesByUserId(userId);
            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, names);
            binding.activityNameInput.setAdapter(nameAdapter);

            List<String> units = repository.findDistinctMeasurementUnitsByUserId(userId);
            ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, units);
            binding.measurementUnitInput.setAdapter(unitAdapter);
        }
    }

    private void updateStats() {
        SportLogRepository repository = MainActivity.getSharedSportLogRepository();
        List<SportLog> allEntries = new ArrayList<>();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
        }

        SportLogStats weeklyStats = sportLogService.getCurrentWeekStats(allEntries);
        SportLogStats monthlyStats = sportLogService.getCurrentMonthStats(allEntries);

        binding.weeklyAvgDurationText.setText(getString(R.string.sport_log_avg_duration, weeklyStats.getAverageDurationHours()));
        binding.weeklyEntriesText.setText(getString(R.string.sport_log_total_entries, weeklyStats.getTotalEntries()));
        binding.monthlyAvgDurationText.setText(getString(R.string.sport_log_avg_duration, monthlyStats.getAverageDurationHours()));
        binding.monthlyEntriesText.setText(getString(R.string.sport_log_total_entries, monthlyStats.getTotalEntries()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
