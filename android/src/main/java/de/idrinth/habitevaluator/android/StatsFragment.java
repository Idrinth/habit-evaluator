package de.idrinth.habitevaluator.android;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.idrinth.habitevaluator.android.databinding.FragmentStatsBinding;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.android.ui.EmotionScatterChartView;

public class StatsFragment extends Fragment {

    private static final int DAYS = 30;
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    private FragmentStatsBinding binding;
    private final DiaryService diaryService = new DiaryService();
    private final HabitScoringService scoringService = new HabitScoringService();
    private final EventCorrelationService correlationService = new EventCorrelationService();

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
        updateEmotionChart(startDate, today, labels);
        updateEmotionScatterChart(startDate, today, labels);
        updateCorrelations();
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
        binding.sleepDurationChart.setValueFormat("%.1f");
        binding.sleepDurationChart.setData(labels, durations, avgDuration);

        binding.sleepEntriesChart.setBarColor(0xFF81C784);
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
        binding.habitPointsChart.setValueFormat("%.0f");
        binding.habitPointsChart.setData(labels, habitPoints, avgPoints);
    }

    private void updateEmotionChart(LocalDate startDate, LocalDate today, List<String> labels) {
        List<EmotionEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedCurrentUser();
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
        User user = MainActivity.getSharedCurrentUser();
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

    private void updateCorrelations() {
        List<Habit> habits = MainActivity.getSharedHabits();
        if (habits == null) {
            habits = new ArrayList<>();
        }

        List<DiaryEntry> diaryEntries = new ArrayList<>();
        List<SleepEntry> sleepEntries = MainActivity.getSharedSleepEntries();
        if (sleepEntries == null) {
            sleepEntries = new ArrayList<>();
        }

        User user = MainActivity.getSharedCurrentUser();
        if (user != null && MainActivity.getSharedDiaryEntryRepository() != null) {
            diaryEntries = MainActivity.getSharedDiaryEntryRepository().findByUserId(user.getId());
        }

        List<EmotionEntry> emotionEntries = new ArrayList<>();
        if (user != null && MainActivity.getSharedEmotionEntryRepository() != null) {
            emotionEntries = MainActivity.getSharedEmotionEntryRepository().findByUserId(user.getId());
        }

        List<EventCorrelation> correlations = correlationService.calculateCorrelations(
                habits, diaryEntries, sleepEntries, emotionEntries);

        binding.correlationContainer.removeAllViews();

        if (correlations.isEmpty()) {
            binding.correlationCard.setVisibility(View.GONE);
            return;
        }

        binding.correlationCard.setVisibility(View.VISIBLE);

        // Add disclaimer
        TextView disclaimer = new TextView(requireContext());
        disclaimer.setText(getString(R.string.stats_correlation_disclaimer));
        disclaimer.setTextSize(11);
        disclaimer.setTextColor(0xFF9E9E9E);
        disclaimer.setPadding(0, 0, 0, 12);
        binding.correlationContainer.addView(disclaimer);

        for (EventCorrelation corr : correlations) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);

            double absCorr = Math.abs(corr.getCorrelation());
            boolean isWeak = absCorr < 0.3;

            if (isWeak) {
                row.setAlpha(0.6f);
            }

            TextView events = new TextView(requireContext());
            events.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            events.setText(corr.getEventA() + " \u2194 " + corr.getEventB());
            events.setTextSize(13);
            row.addView(events);

            TextView confidence = new TextView(requireContext());
            confidence.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (absCorr >= 0.5) {
                confidence.setText(getString(R.string.stats_confidence_strong));
                confidence.setTextColor(0xFF4CAF50);
            } else if (absCorr >= 0.3) {
                confidence.setText(getString(R.string.stats_confidence_moderate));
                confidence.setTextColor(0xFFFF9800);
            } else {
                confidence.setText(getString(R.string.stats_confidence_weak));
                confidence.setTextColor(0xFF9E9E9E);
            }
            confidence.setTextSize(11);
            confidence.setPadding(8, 0, 8, 0);
            confidence.setGravity(Gravity.END);
            row.addView(confidence);

            TextView value = new TextView(requireContext());
            value.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            value.setText(String.format(Locale.US, "%+.3f", corr.getCorrelation()));
            value.setTextSize(13);
            value.setTypeface(Typeface.MONOSPACE);
            value.setGravity(Gravity.END);
            if (corr.getCorrelation() > 0) {
                value.setTextColor(0xFF4CAF50);
            } else {
                value.setTextColor(0xFFE91E63);
            }
            row.addView(value);

            binding.correlationContainer.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
