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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentSleepTrackingBinding;
import de.idrinth.habitevaluator.android.ui.SleepEntryAdapter;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SleepStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;

public class SleepTrackingFragment extends Fragment implements SleepEntryAdapter.OnSleepEntryDeleteListener {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private FragmentSleepTrackingBinding binding;
    private SleepEntryAdapter adapter;
    private List<SleepEntry> displayedEntries;
    private SleepEvaluationService evaluationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSleepTrackingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayedEntries = new ArrayList<>();
        evaluationService = new SleepEvaluationService();

        setupRecyclerView();
        binding.addSleepEntryButton.setOnClickListener(v -> addSleepEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void setupRecyclerView() {
        adapter = new SleepEntryAdapter(displayedEntries, this);
        binding.sleepEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.sleepEntriesRecyclerView.setAdapter(adapter);
    }

    private void addSleepEntry() {
        String fromText = binding.sleepFromInput.getText().toString().trim();
        String untilText = binding.sleepUntilInput.getText().toString().trim();

        if (fromText.isEmpty() || untilText.isEmpty()) {
            Toast.makeText(requireContext(), R.string.sleep_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        LocalTime fromTime;
        LocalTime untilTime;
        try {
            fromTime = LocalTime.parse(fromText, TIME_FORMAT);
            untilTime = LocalTime.parse(untilText, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), R.string.sleep_time_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = MainActivity.getSharedCurrentUser();
        SleepEntry entry = new SleepEntry(fromTime, untilTime, LocalDate.now());
        entry.setUser(currentUser);

        String notes = binding.sleepNotesInput.getText().toString().trim();
        if (!notes.isEmpty()) {
            entry.setNotes(notes);
        }

        SleepEntryRepository repository = MainActivity.getSharedSleepEntryRepository();
        if (repository != null) {
            repository.save(entry);
        }

        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        if (allEntries != null) {
            allEntries.add(entry);
        }

        binding.sleepFromInput.setText("");
        binding.sleepUntilInput.setText("");
        binding.sleepNotesInput.setText("");

        Toast.makeText(requireContext(), R.string.sleep_entry_added, Toast.LENGTH_SHORT).show();
        refreshData();
    }

    @Override
    public void onDelete(SleepEntry entry) {
        SleepEntryRepository repository = MainActivity.getSharedSleepEntryRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }

        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        if (allEntries != null) {
            allEntries.remove(entry);
        }

        Toast.makeText(requireContext(), R.string.sleep_entry_removed, Toast.LENGTH_SHORT).show();
        refreshData();
    }

    private void refreshData() {
        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        displayedEntries.clear();
        if (allEntries != null) {
            List<SleepEntry> sorted = new ArrayList<>(allEntries);
            sorted.sort(Comparator.comparing(SleepEntry::getDate).reversed());
            displayedEntries.addAll(sorted);
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }

    private void updateStats() {
        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        if (allEntries == null) {
            allEntries = new ArrayList<>();
        }

        SleepStats weeklyStats = evaluationService.getCurrentWeekStats(allEntries);
        SleepStats monthlyStats = evaluationService.getCurrentMonthStats(allEntries);

        if (weeklyStats.getTotalEntries() > 0) {
            binding.weeklyAvgText.setText(getString(R.string.sleep_avg, weeklyStats.getAverageHours()));
            binding.weeklyMinText.setText(getString(R.string.sleep_min, weeklyStats.getMinHours()));
            binding.weeklyMaxText.setText(getString(R.string.sleep_max, weeklyStats.getMaxHours()));
        } else {
            binding.weeklyAvgText.setText(getString(R.string.sleep_avg, 0.0));
            binding.weeklyMinText.setText(getString(R.string.sleep_min, 0.0));
            binding.weeklyMaxText.setText(getString(R.string.sleep_max, 0.0));
        }

        if (monthlyStats.getTotalEntries() > 0) {
            binding.monthlyAvgText.setText(getString(R.string.sleep_avg, monthlyStats.getAverageHours()));
            binding.monthlyMinText.setText(getString(R.string.sleep_min, monthlyStats.getMinHours()));
            binding.monthlyMaxText.setText(getString(R.string.sleep_max, monthlyStats.getMaxHours()));
        } else {
            binding.monthlyAvgText.setText(getString(R.string.sleep_avg, 0.0));
            binding.monthlyMinText.setText(getString(R.string.sleep_min, 0.0));
            binding.monthlyMaxText.setText(getString(R.string.sleep_max, 0.0));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
