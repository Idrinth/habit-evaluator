package de.idrinth.habitevaluator.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.idrinth.habitevaluator.android.databinding.ActivitySleepAnalysisBinding;
import de.idrinth.habitevaluator.shared.model.SleepEntry;

public class SleepAnalysisActivity extends AppCompatActivity {

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySleepAnalysisBinding binding = ActivitySleepAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle(R.string.sleep_analysis_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        if (allEntries == null) {
            allEntries = new ArrayList<>();
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);

        Map<LocalDate, List<SleepEntry>> entriesByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            entriesByDate.put(d, new ArrayList<>());
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                entriesByDate.get(entry.getDate()).add(entry);
            }
        }

        List<String> labels = new ArrayList<>();
        List<Float> durations = new ArrayList<>();
        List<Float> interruptions = new ArrayList<>();
        float totalDuration = 0f;
        float totalInterruptions = 0f;
        int daysWithData = 0;

        for (Map.Entry<LocalDate, List<SleepEntry>> mapEntry : entriesByDate.entrySet()) {
            labels.add(mapEntry.getKey().format(LABEL_FORMAT));
            List<SleepEntry> dayEntries = mapEntry.getValue();

            float dayDuration = 0f;
            for (SleepEntry entry : dayEntries) {
                dayDuration += (float) entry.getHours();
            }
            durations.add(dayDuration);

            int dayInterruptions = Math.max(0, dayEntries.size() - 1);
            interruptions.add((float) dayInterruptions);

            if (!dayEntries.isEmpty()) {
                totalDuration += dayDuration;
                totalInterruptions += dayInterruptions;
                daysWithData++;
            }
        }

        float avgDuration = daysWithData > 0 ? totalDuration / daysWithData : 0f;
        float avgInterruptions = daysWithData > 0 ? totalInterruptions / daysWithData : 0f;

        binding.durationGraphView.setBarColor(0xFF4A90D9);
        binding.durationGraphView.setAverageColor(0xFFE05050);
        binding.durationGraphView.setValueFormat("%.1f");
        binding.durationGraphView.setData(labels, durations, avgDuration);

        binding.interruptionsGraphView.setBarColor(0xFFE8A838);
        binding.interruptionsGraphView.setAverageColor(0xFFE05050);
        binding.interruptionsGraphView.setValueFormat("%.0f");
        binding.interruptionsGraphView.setData(labels, interruptions, avgInterruptions);
    }
}
