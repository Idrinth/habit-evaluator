package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import de.idrinth.habitevaluator.android.databinding.ActivityCorrelationBinding;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;

public class CorrelationActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(FontSizeHelper.applyFontScale(newBase));
    }

    private ActivityCorrelationBinding binding;
    private final EventCorrelationService correlationService = new EventCorrelationService();
    private List<EventCorrelation> allCorrelations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCorrelationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle(R.string.stats_correlations_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        computeCorrelations();
        populateFilters();
        displayCorrelations(allCorrelations);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        };
        binding.sourceFilter.setOnItemSelectedListener(filterListener);
        binding.targetFilter.setOnItemSelectedListener(filterListener);
    }

    private void computeCorrelations() {
        List<Habit> habits = MainActivity.getSharedHabits();
        if (habits == null) {
            habits = new ArrayList<>();
        }

        List<DiaryEntry> diaryEntries = new ArrayList<>();
        List<SleepEntry> sleepEntries = MainActivity.getSharedSleepEntries();
        if (sleepEntries == null) {
            sleepEntries = new ArrayList<>();
        }

        User user = MainActivity.getSharedLocalUser();
        if (user != null && MainActivity.getSharedDiaryEntryRepository() != null) {
            diaryEntries = MainActivity.getSharedDiaryEntryRepository().findByUserId(user.getId());
        }

        List<EmotionEntry> emotionEntries = new ArrayList<>();
        if (user != null && MainActivity.getSharedEmotionEntryRepository() != null) {
            emotionEntries = MainActivity.getSharedEmotionEntryRepository().findByUserId(user.getId());
        }

        List<SportLog> sportLogs = new ArrayList<>();
        if (user != null && MainActivity.getSharedSportLogRepository() != null) {
            sportLogs = MainActivity.getSharedSportLogRepository().findByUserId(user.getId());
        }

        List<FoodLog> foodLogs = new ArrayList<>();
        if (user != null && MainActivity.getSharedFoodLogRepository() != null) {
            foodLogs = MainActivity.getSharedFoodLogRepository().findByUserId(user.getId());
        }

        List<MedicationLog> medicationLogs = new ArrayList<>();
        if (user != null && MainActivity.getSharedMedicationLogRepository() != null) {
            medicationLogs = MainActivity.getSharedMedicationLogRepository().findByUserId(user.getId());
        }

        allCorrelations = correlationService.calculateCorrelations(
                habits, diaryEntries, sleepEntries, emotionEntries,
                sportLogs, new ArrayList<>(), new ArrayList<>(),
                foodLogs, medicationLogs);
    }

    private void populateFilters() {
        TreeSet<String> eventNames = new TreeSet<>();
        for (EventCorrelation corr : allCorrelations) {
            eventNames.add(corr.getEventA());
            eventNames.add(corr.getEventB());
        }

        String allLabel = getString(R.string.stats_filter_all);
        List<String> filterOptions = new ArrayList<>();
        filterOptions.add(allLabel);
        filterOptions.addAll(eventNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.sourceFilter.setAdapter(adapter);
        binding.targetFilter.setAdapter(adapter);
    }

    private void applyFilters() {
        String allLabel = getString(R.string.stats_filter_all);
        String sourceSelection = (String) binding.sourceFilter.getSelectedItem();
        String targetSelection = (String) binding.targetFilter.getSelectedItem();

        if (sourceSelection == null) {
            sourceSelection = allLabel;
        }
        if (targetSelection == null) {
            targetSelection = allLabel;
        }

        displayCorrelations(filterCorrelations(allCorrelations, allLabel, sourceSelection, targetSelection));
    }

    static List<EventCorrelation> filterCorrelations(
            List<EventCorrelation> correlations, String allLabel,
            String sourceSelection, String targetSelection) {
        List<EventCorrelation> filtered = new ArrayList<>();
        for (EventCorrelation corr : correlations) {
            boolean matchesSource = allLabel.equals(sourceSelection)
                    || corr.getEventA().equals(sourceSelection);
            boolean matchesTarget = allLabel.equals(targetSelection)
                    || corr.getEventB().equals(targetSelection);
            if (matchesSource && matchesTarget) {
                filtered.add(corr);
            }
        }
        return filtered;
    }

    private void displayCorrelations(List<EventCorrelation> correlations) {
        binding.correlationContainer.removeAllViews();

        if (correlations.isEmpty()) {
            TextView noData = new TextView(this);
            noData.setText(getString(R.string.stats_no_correlations));
            noData.setTextColor(0xFF9E9E9E);
            noData.setTextSize(14);
            noData.setPadding(0, 16, 0, 0);
            binding.correlationContainer.addView(noData);
            return;
        }

        TextView disclaimer = new TextView(this);
        disclaimer.setText(getString(R.string.stats_correlation_disclaimer));
        disclaimer.setTextSize(11);
        disclaimer.setTextColor(0xFF9E9E9E);
        disclaimer.setPadding(0, 0, 0, 12);
        binding.correlationContainer.addView(disclaimer);

        for (EventCorrelation corr : correlations) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);

            double absCorr = Math.abs(corr.getCorrelation());
            boolean isWeak = absCorr < 0.3;

            if (isWeak) {
                row.setAlpha(0.6f);
            }

            TextView events = new TextView(this);
            events.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            events.setText(corr.getEventA() + " \u2194 " + corr.getEventB());
            events.setTextSize(13);
            row.addView(events);

            TextView confidence = new TextView(this);
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

            TextView value = new TextView(this);
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
}
