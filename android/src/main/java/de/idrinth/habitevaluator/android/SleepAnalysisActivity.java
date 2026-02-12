package de.idrinth.habitevaluator.android;

import android.content.Context;
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(FontSizeHelper.applyFontScale(newBase));
    }

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

            int dayInterruptions = calculateInterruptions(dayEntries.size());
            interruptions.add((float) dayInterruptions);

            if (!dayEntries.isEmpty()) {
                totalDuration += dayDuration;
                totalInterruptions += dayInterruptions;
                daysWithData++;
            }
        }

        float avgDuration = calculateAverage(totalDuration, daysWithData);
        float avgInterruptions = calculateAverage(totalInterruptions, daysWithData);

        binding.durationGraphView.setBarColor(0xFF4CAF50);
        binding.durationGraphView.setValueFormat("%.1f");
        binding.durationGraphView.setData(labels, durations, avgDuration);

        binding.interruptionsGraphView.setBarColor(0xFF81C784);
        binding.interruptionsGraphView.setValueFormat("%.0f");
        binding.interruptionsGraphView.setData(labels, interruptions, avgInterruptions);
    }

    /**
     * Calculates sleep interruptions for a day. Multiple sleep segments
     * indicate interrupted sleep (e.g., 3 segments = 2 interruptions).
     */
    static int calculateInterruptions(int segmentCount) {
        return Math.max(0, segmentCount - 1);
    }

    /**
     * Calculates the average value, returning 0 when there is no data.
     */
    static float calculateAverage(float total, int count) {
        return count > 0 ? total / count : 0f;
    }
}
