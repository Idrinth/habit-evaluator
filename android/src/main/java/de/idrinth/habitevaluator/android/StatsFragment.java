package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.idrinth.habitevaluator.android.databinding.FragmentStatsBinding;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;

public class StatsFragment extends Fragment {

    private static final int DAYS = 30;
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    private FragmentStatsBinding binding;
    private final DiaryService diaryService = new DiaryService();
    private final HabitScoringService scoringService = new HabitScoringService();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCharts();
    }

    private void updateCharts() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS - 1);

        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }

        updateSleepCharts(startDate, today, labels);
        updateDiaryChart(startDate, today, labels);
        updateHabitChart(startDate, today, labels);
    }

    private void updateSleepCharts(LocalDate startDate, LocalDate today, List<String> labels) {
        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        if (allEntries == null) {
            allEntries = new ArrayList<>();
        }

        Map<LocalDate, List<SleepEntry>> entriesByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            entriesByDate.put(d, new ArrayList<>());
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                entriesByDate.get(entry.getDate()).add(entry);
            }
        }

        List<Float> durations = new ArrayList<>();
        List<Float> entryCounts = new ArrayList<>();
        float totalDuration = 0f;
        float totalEntries = 0f;
        int daysWithData = 0;

        for (Map.Entry<LocalDate, List<SleepEntry>> mapEntry : entriesByDate.entrySet()) {
            List<SleepEntry> dayEntries = mapEntry.getValue();

            float dayDuration = 0f;
            for (SleepEntry entry : dayEntries) {
                dayDuration += (float) entry.getHours();
            }
            durations.add(dayDuration);
            entryCounts.add((float) dayEntries.size());

            if (!dayEntries.isEmpty()) {
                totalDuration += dayDuration;
                totalEntries += dayEntries.size();
                daysWithData++;
            }
        }

        float avgDuration = daysWithData > 0 ? totalDuration / daysWithData : 0f;
        float avgEntries = daysWithData > 0 ? totalEntries / daysWithData : 0f;

        binding.sleepDurationChart.setBarColor(0xFF4CAF50);
        binding.sleepDurationChart.setAverageColor(0xFF1B5E20);
        binding.sleepDurationChart.setValueFormat("%.1f");
        binding.sleepDurationChart.setData(labels, durations, avgDuration);

        binding.sleepEntriesChart.setBarColor(0xFF81C784);
        binding.sleepEntriesChart.setAverageColor(0xFF1B5E20);
        binding.sleepEntriesChart.setValueFormat("%.0f");
        binding.sleepEntriesChart.setData(labels, entryCounts, avgEntries);
    }

    private void updateDiaryChart(LocalDate startDate, LocalDate today, List<String> labels) {
        List<DiaryEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedCurrentUser();
        if (user != null && MainActivity.getSharedDiaryEntryRepository() != null) {
            allEntries = MainActivity.getSharedDiaryEntryRepository().findByUserId(user.getId());
        }

        List<Float> diaryPoints = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            int dayPoints = diaryService.getDayPoints(allEntries, d);
            diaryPoints.add((float) dayPoints);
            if (dayPoints > 0) {
                totalPoints += dayPoints;
                daysWithData++;
            }
        }

        float avgPoints = daysWithData > 0 ? totalPoints / daysWithData : 0f;

        binding.diaryPointsChart.setBarColor(0xFF66BB6A);
        binding.diaryPointsChart.setAverageColor(0xFF1B5E20);
        binding.diaryPointsChart.setValueFormat("%.0f");
        binding.diaryPointsChart.setData(labels, diaryPoints, avgPoints);
    }

    private void updateHabitChart(LocalDate startDate, LocalDate today, List<String> labels) {
        List<Habit> habits = MainActivity.getSharedHabits();
        if (habits == null) {
            habits = new ArrayList<>();
        }

        List<Float> habitPoints = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            int dayTotal = 0;
            for (Habit habit : habits) {
                dayTotal += scoringService.calculateHabitScore(habit, d, d).getScore();
            }
            habitPoints.add((float) dayTotal);
            if (dayTotal > 0) {
                totalPoints += dayTotal;
                daysWithData++;
            }
        }

        float avgPoints = daysWithData > 0 ? totalPoints / daysWithData : 0f;

        binding.habitPointsChart.setBarColor(0xFF388E3C);
        binding.habitPointsChart.setAverageColor(0xFF1B5E20);
        binding.habitPointsChart.setValueFormat("%.0f");
        binding.habitPointsChart.setData(labels, habitPoints, avgPoints);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
