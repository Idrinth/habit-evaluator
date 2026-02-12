package de.idrinth.habitevaluator.android;

import android.content.Intent;
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
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.android.ui.EmotionScatterChartView;

public class StatsFragment extends Fragment {

    static final int DAYS = 30;
    static final String LABEL_PATTERN = "MM/dd";
    static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern(LABEL_PATTERN);

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.exportPdfButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PdfExportActivity.class);
            startActivity(intent);
        });
        binding.viewCorrelationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CorrelationActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCharts();
    }

    private void updateCharts() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS - 1);

        List<String> labels = generateDateLabels(startDate, today);

        updateSleepCharts(startDate, today, labels);
        updateDiaryChart(startDate, today, labels);
        updateHabitChart(startDate, today, labels);
        updateEmotionChart(startDate, today, labels);
        updateEmotionScatterChart(startDate, today, labels);
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

        float avgDuration = calculateAverage(totalDuration, daysWithData);
        float avgEntries = calculateAverage(totalEntries, daysWithData);

        binding.sleepDurationChart.setBarColor(0xFF4CAF50);
        binding.sleepDurationChart.setValueFormat("%.1f");
        binding.sleepDurationChart.setData(labels, durations, avgDuration);

        binding.sleepEntriesChart.setBarColor(0xFF81C784);
        binding.sleepEntriesChart.setValueFormat("%.0f");
        binding.sleepEntriesChart.setData(labels, entryCounts, avgEntries);
    }

    private void updateDiaryChart(LocalDate startDate, LocalDate today, List<String> labels) {
        List<DiaryEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedLocalUser();
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

        float avgPoints = calculateAverage(totalPoints, daysWithData);

        binding.diaryPointsChart.setBarColor(0xFF66BB6A);
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

        float avgPoints = calculateAverage(totalPoints, daysWithData);

        binding.habitPointsChart.setBarColor(0xFF388E3C);
        binding.habitPointsChart.setValueFormat("%.0f");
        binding.habitPointsChart.setData(labels, habitPoints, avgPoints);
    }

    private void updateEmotionChart(LocalDate startDate, LocalDate today, List<String> labels) {
        List<EmotionEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedLocalUser();
        if (user != null && MainActivity.getSharedEmotionEntryRepository() != null) {
            allEntries = MainActivity.getSharedEmotionEntryRepository().findByUserId(user.getId());
        }

        if (allEntries.isEmpty()) {
            binding.emotionCard.setVisibility(View.GONE);
            return;
        }

        binding.emotionCard.setVisibility(View.VISIBLE);

        // Find earliest entry date to determine full range
        LocalDate earliest = today;
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (entryDate.isBefore(earliest)) {
                earliest = entryDate;
            }
        }

        // Build full date range labels
        List<String> emotionLabels = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = earliest; !d.isAfter(today); d = d.plusDays(1)) {
            emotionLabels.add(d.format(LABEL_FORMAT));
            dates.add(d);
        }

        // Group entries by emotion pair
        Map<String, List<EmotionEntry>> entriesByPair = new TreeMap<>();
        Map<String, String> pairLabelMap = new TreeMap<>();
        for (EmotionEntry entry : allEntries) {
            if (entry.getEmotionPair() != null) {
                String pairId = entry.getEmotionPair().getId();
                entriesByPair.computeIfAbsent(pairId, k -> new ArrayList<>()).add(entry);
                pairLabelMap.put(pairId, entry.getEmotionPair().toString());
            }
        }

        // For each pair, compute daily average strength
        List<String> pairNames = new ArrayList<>();
        List<List<Float>> pairDailyValues = new ArrayList<>();
        for (Map.Entry<String, List<EmotionEntry>> mapEntry : entriesByPair.entrySet()) {
            pairNames.add(pairLabelMap.get(mapEntry.getKey()));
            List<EmotionEntry> pairEntries = mapEntry.getValue();
            Map<LocalDate, List<Integer>> strengthsByDate = new TreeMap<>();
            for (EmotionEntry entry : pairEntries) {
                LocalDate d = entry.getRecordedAt().toLocalDate();
                strengthsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(entry.getStrength());
            }
            List<Float> dailyAvgs = new ArrayList<>();
            for (LocalDate d : dates) {
                List<Integer> strengths = strengthsByDate.get(d);
                if (strengths != null && !strengths.isEmpty()) {
                    float sum = 0;
                    for (int s : strengths) {
                        sum += s;
                    }
                    dailyAvgs.add(sum / strengths.size());
                } else {
                    dailyAvgs.add(null);
                }
            }
            pairDailyValues.add(dailyAvgs);
        }

        binding.emotionChart.setData(emotionLabels, pairNames, pairDailyValues);
    }

    private void updateEmotionScatterChart(LocalDate startDate, LocalDate today, List<String> labels) {
        List<EmotionEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedLocalUser();
        if (user != null && MainActivity.getSharedEmotionEntryRepository() != null) {
            allEntries = MainActivity.getSharedEmotionEntryRepository().findByUserId(user.getId());
        }

        // Filter entries within the date range
        List<EmotionEntry> rangeEntries = new ArrayList<>();
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                rangeEntries.add(entry);
            }
        }

        if (rangeEntries.isEmpty()) {
            binding.emotionScatterCard.setVisibility(View.GONE);
            return;
        }

        binding.emotionScatterCard.setVisibility(View.VISIBLE);

        // Group entries by emotion pair
        Map<String, List<EmotionEntry>> entriesByPair = new TreeMap<>();
        Map<String, String> pairLabelMap = new TreeMap<>();
        for (EmotionEntry entry : rangeEntries) {
            if (entry.getEmotionPair() != null) {
                String pairId = entry.getEmotionPair().getId();
                entriesByPair.computeIfAbsent(pairId, k -> new ArrayList<>()).add(entry);
                pairLabelMap.put(pairId, entry.getEmotionPair().toString());
            }
        }

        // Build scatter pairs using time of day
        List<EmotionScatterChartView.ScatterPair> scatterPairs = new ArrayList<>();
        for (Map.Entry<String, List<EmotionEntry>> mapEntry : entriesByPair.entrySet()) {
            String pairLabel = pairLabelMap.get(mapEntry.getKey());
            List<EmotionScatterChartView.ScatterEntry> scatterEntries = new ArrayList<>();

            for (EmotionEntry entry : mapEntry.getValue()) {
                // Extract hour of day (including minutes as decimal)
                float hourOfDay = entry.getRecordedAt().getHour()
                        + entry.getRecordedAt().getMinute() / 60f;
                float strength = entry.getStrength();
                scatterEntries.add(new EmotionScatterChartView.ScatterEntry(hourOfDay, strength));
            }

            scatterPairs.add(new EmotionScatterChartView.ScatterPair(pairLabel, scatterEntries));
        }

        binding.emotionScatterChart.setData(scatterPairs);
    }

    static float calculateAverage(float total, int count) {
        return count > 0 ? total / count : 0f;
    }

    static List<String> generateDateLabels(LocalDate startDate, LocalDate endDate) {
        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }
        return labels;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
