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

import de.idrinth.habitevaluator.android.databinding.FragmentDiaryBinding;
import de.idrinth.habitevaluator.android.ui.DiaryEntryAdapter;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;

public class DiaryFragment extends Fragment {

    static final String DATE_PATTERN = "yyyy-MM-dd";
    static final String TIME_PATTERN = "HH:mm";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private FragmentDiaryBinding binding;
    private DiaryEntryAdapter adapter;
    private List<DiaryEntry> displayedEntries;
    private DiaryService diaryService;
    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDiaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayedEntries = new ArrayList<>();
        diaryService = new DiaryService();
        selectedDate = LocalDate.now();

        setupRecyclerView();
        setupSignificanceSpinner();
        setupDatePicker();
        setupTimePickers();
        setupAddButton();
        setupFormToggle();
        loadEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEntries();
    }

    private void setupRecyclerView() {
        adapter = new DiaryEntryAdapter(displayedEntries, this::deleteDiaryEntry);
        binding.diaryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.diaryRecyclerView.setAdapter(adapter);
    }

    private void deleteDiaryEntry(DiaryEntry entry) {
        DiaryEntryRepository repository = MainActivity.getSharedDiaryEntryRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }
        loadEntries();
        Toast.makeText(requireContext(), R.string.diary_entry_removed, Toast.LENGTH_SHORT).show();
    }

    private void setupSignificanceSpinner() {
        String[] labels = {
                getString(R.string.diary_minor) + " (1 pt)",
                getString(R.string.diary_normal) + " (2 pts)",
                getString(R.string.diary_major) + " (4 pts)"
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.significanceSpinner.setAdapter(spinnerAdapter);
        binding.significanceSpinner.setSelection(1); // Default to NORMAL
    }

    private void setupDatePicker() {
        binding.eventDateInput.setText(selectedDate.format(DATE_FORMAT));
        binding.eventDateInput.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    binding.eventDateInput.setText(selectedDate.format(DATE_FORMAT));
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupTimePickers() {
        binding.startTimeInput.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeInput.setOnClickListener(v -> showTimePicker(false));
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
                        binding.startTimeInput.setText(time.format(TIME_FORMAT));
                    } else {
                        selectedEndTime = time;
                        binding.endTimeInput.setText(time.format(TIME_FORMAT));
                    }
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void setupAddButton() {
        binding.addEventButton.setOnClickListener(v -> addEvent());
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

    private void addEvent() {
        String description = binding.eventDescriptionInput.getText() != null
                ? binding.eventDescriptionInput.getText().toString().trim() : "";

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), R.string.diary_description_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = binding.significanceSpinner.getSelectedItemPosition();
        EventSignificance significance = mapPositionToSignificance(selectedPosition);

        DiaryEntry entry = new DiaryEntry(description, significance, selectedDate);
        entry.setUser(MainActivity.getSharedLocalUser());
        entry.setStartTime(selectedStartTime);
        entry.setEndTime(selectedEndTime);

        DiaryEntryRepository repository = MainActivity.getSharedDiaryEntryRepository();
        if (repository != null) {
            repository.save(entry);
        }

        binding.eventDescriptionInput.setText("");
        selectedDate = LocalDate.now();
        binding.eventDateInput.setText(selectedDate.format(DATE_FORMAT));
        selectedStartTime = null;
        selectedEndTime = null;
        binding.startTimeInput.setText("");
        binding.endTimeInput.setText("");
        binding.addEntryFormContainer.setVisibility(View.GONE);
        binding.toggleFormButton.setImageResource(android.R.drawable.arrow_down_float);
        loadEntries();
        Toast.makeText(requireContext(), R.string.diary_event_added, Toast.LENGTH_SHORT).show();
    }

    private void loadEntries() {
        displayedEntries.clear();

        DiaryEntryRepository repository = MainActivity.getSharedDiaryEntryRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<DiaryEntry> allEntries = repository.findByUserId(MainActivity.getSharedLocalUser().getId());
            allEntries.sort(Comparator.comparing(DiaryEntry::getEventDate).reversed()
                    .thenComparing(Comparator.comparing(DiaryEntry::getCreatedAt).reversed()));
            displayedEntries.addAll(allEntries);
        }

        adapter.notifyDataSetChanged();
        updateStats();
        updateSuggestions();
    }

    private void updateSuggestions() {
        DiaryEntryRepository repository = MainActivity.getSharedDiaryEntryRepository();
        if (repository != null && MainActivity.getSharedLocalUser() != null) {
            List<String> suggestions = repository.findDistinctDescriptionsByUserId(
                    MainActivity.getSharedLocalUser().getId());
            ArrayAdapter<String> suggestionsAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            binding.eventDescriptionInput.setAdapter(suggestionsAdapter);
        }
    }

    private void updateStats() {
        binding.todayPointsText.setText(getString(R.string.diary_today_points,
                diaryService.getDayPoints(displayedEntries, LocalDate.now())));

        binding.weekPointsText.setText(getString(R.string.diary_week_points,
                diaryService.getCurrentWeekPoints(displayedEntries)));

        binding.monthPointsText.setText(getString(R.string.diary_month_points,
                diaryService.getCurrentMonthPoints(displayedEntries)));

        double weeklyAvg = diaryService.getWeeklyAverageForMonth(displayedEntries);
        binding.weeklyAvgText.setText(getString(R.string.diary_weekly_avg, weeklyAvg));

        double dailyAvg = diaryService.getDailyAverageForMonth(displayedEntries);
        binding.dailyAvgText.setText(getString(R.string.diary_daily_avg, dailyAvg));

        double trend = diaryService.getMonthlyTrend(displayedEntries);
        String trendText;
        if (trend > 0.05) {
            trendText = getString(R.string.diary_trend_up, Math.abs(trend * 100));
        } else if (trend < -0.05) {
            trendText = getString(R.string.diary_trend_down, Math.abs(trend * 100));
        } else {
            trendText = getString(R.string.diary_trend_stable);
        }
        binding.monthlyTrendText.setText(trendText);
    }

    static EventSignificance mapPositionToSignificance(int position) {
        switch (position) {
            case 0:
                return EventSignificance.MINOR;
            case 2:
                return EventSignificance.MAJOR;
            default:
                return EventSignificance.NORMAL;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
