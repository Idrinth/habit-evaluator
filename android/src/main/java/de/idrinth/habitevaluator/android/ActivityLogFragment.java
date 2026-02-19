package de.idrinth.habitevaluator.android;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import de.idrinth.habitevaluator.android.databinding.FragmentActivityLogBinding;
import de.idrinth.habitevaluator.android.ui.ActivityLogAdapter;
import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;

public class ActivityLogFragment extends Fragment implements ActivityLogAdapter.OnActivityLogDeleteListener {

    static final String DATE_PATTERN = "yyyy-MM-dd";
    static final String TIME_PATTERN = "HH:mm";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private FragmentActivityLogBinding binding;
    private ActivityLogAdapter adapter;
    private List<ActivityLog> displayedEntries;
    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayedEntries = new ArrayList<>();
        selectedDate = LocalDate.now();

        setupRecyclerView();
        setupDatePicker();
        setupTimePickers();
        setupFormToggle();
        binding.addActivityLogButton.setOnClickListener(v -> addActivityLog());
        loadEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEntries();
    }

    private void setupRecyclerView() {
        adapter = new ActivityLogAdapter(displayedEntries, this);
        binding.activityLogRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.activityLogRecyclerView.setAdapter(adapter);
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
        binding.activityDateInput.setText(selectedDate.format(DATE_FORMAT));
        binding.activityDateInput.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    binding.activityDateInput.setText(selectedDate.format(DATE_FORMAT));
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupTimePickers() {
        binding.activityStartTimeInput.setOnClickListener(v -> showTimePicker(true));
        binding.activityEndTimeInput.setOnClickListener(v -> showTimePicker(false));
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
                        binding.activityStartTimeInput.setText(time.format(TIME_FORMAT));
                    } else {
                        selectedEndTime = time;
                        binding.activityEndTimeInput.setText(time.format(TIME_FORMAT));
                    }
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void addActivityLog() {
        String persons = binding.personsInput.getText() != null
                ? binding.personsInput.getText().toString().trim() : "";
        String location = binding.locationInput.getText() != null
                ? binding.locationInput.getText().toString().trim() : "";

        if (persons.isEmpty()) {
            Toast.makeText(requireContext(), R.string.activity_log_persons_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty()) {
            Toast.makeText(requireContext(), R.string.activity_log_location_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(requireContext(), R.string.activity_log_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        ActivityLog entry = new ActivityLog(persons, location, selectedStartTime, selectedEndTime, selectedDate);
        entry.setUser(MainActivity.getSharedLocalUser());

        String activity = binding.activityInput.getText() != null
                ? binding.activityInput.getText().toString().trim() : "";
        if (!activity.isEmpty()) {
            entry.setActivity(activity);
        }

        ActivityLogRepository repository = MainActivity.getSharedActivityLogRepository();
        if (repository != null) {
            repository.save(entry);
        }

        binding.personsInput.setText("");
        binding.locationInput.setText("");
        binding.activityInput.setText("");
        selectedStartTime = null;
        selectedEndTime = null;
        binding.activityStartTimeInput.setText("");
        binding.activityEndTimeInput.setText("");
        selectedDate = LocalDate.now();
        binding.activityDateInput.setText(selectedDate.format(DATE_FORMAT));

        binding.addEntryFormContainer.setVisibility(View.GONE);
        binding.toggleFormButton.setImageResource(android.R.drawable.arrow_down_float);

        Toast.makeText(requireContext(), R.string.activity_log_entry_added, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    @Override
    public void onDelete(ActivityLog entry) {
        ActivityLogRepository repository = MainActivity.getSharedActivityLogRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }
        Toast.makeText(requireContext(), R.string.activity_log_entry_removed, Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    private void loadEntries() {
        displayedEntries.clear();

        ActivityLogRepository repository = MainActivity.getSharedActivityLogRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<ActivityLog> allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
            allEntries.sort(Comparator.comparing(ActivityLog::getDate).reversed()
                    .thenComparing(Comparator.comparing(ActivityLog::getCreatedAt).reversed()));
            displayedEntries.addAll(allEntries);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
