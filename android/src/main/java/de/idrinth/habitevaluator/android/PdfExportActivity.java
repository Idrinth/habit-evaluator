package de.idrinth.habitevaluator.android;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.idrinth.habitevaluator.android.databinding.ActivityPdfExportBinding;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;

public class PdfExportActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final float MARGIN = 40f;

    private ActivityPdfExportBinding binding;
    private LocalDate fromDate;
    private LocalDate toDate;
    private final DiaryService diaryService = new DiaryService();
    private final HabitScoringService scoringService = new HabitScoringService();
    private final EventCorrelationService correlationService = new EventCorrelationService();

    private final ActivityResultLauncher<Intent> saveFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        writePdfToUri(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfExportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle(R.string.pdf_export_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        toDate = LocalDate.now();
        fromDate = toDate.minusDays(29);
        updateDateButtons();

        binding.fromDateButton.setOnClickListener(v -> showDatePicker(true));
        binding.toDateButton.setOnClickListener(v -> showDatePicker(false));
        binding.exportButton.setOnClickListener(v -> startExport());
    }

    private void updateDateButtons() {
        binding.fromDateButton.setText(fromDate.format(DISPLAY_FORMAT));
        binding.toDateButton.setText(toDate.format(DISPLAY_FORMAT));
    }

    private void showDatePicker(boolean isFromDate) {
        LocalDate current = isFromDate ? fromDate : toDate;
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selected = LocalDate.of(year, month + 1, dayOfMonth);
                    if (isFromDate) {
                        fromDate = selected;
                        if (fromDate.isAfter(toDate)) {
                            toDate = fromDate;
                        }
                    } else {
                        toDate = selected;
                        if (toDate.isBefore(fromDate)) {
                            fromDate = toDate;
                        }
                    }
                    updateDateButtons();
                },
                current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth());
        dialog.show();
    }

    private void startExport() {
        if (!binding.includeHabitsCheckbox.isChecked()
                && !binding.includeDiaryCheckbox.isChecked()
                && !binding.includeSleepCheckbox.isChecked()
                && !binding.includeEmotionsCheckbox.isChecked()) {
            Toast.makeText(this, R.string.pdf_select_content, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String fileName = "habit_report_" + fromDate.format(DISPLAY_FORMAT) + "_to_" + toDate.format(DISPLAY_FORMAT) + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        saveFileLauncher.launch(intent);
    }

    private void writePdfToUri(Uri uri) {
        binding.statusText.setVisibility(View.VISIBLE);
        binding.statusText.setText(R.string.pdf_generating);
        binding.statusText.setTextColor(getColor(R.color.text_secondary));
        binding.exportButton.setEnabled(false);

        new Thread(() -> {
            try {
                PdfDocument document = generatePdf();
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        document.writeTo(os);
                    }
                }
                document.close();
                runOnUiThread(() -> {
                    binding.statusText.setText(R.string.pdf_export_success);
                    binding.statusText.setTextColor(getColor(android.R.color.holo_green_dark));
                    binding.exportButton.setEnabled(true);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    binding.statusText.setText(getString(R.string.pdf_export_failed, e.getMessage()));
                    binding.statusText.setTextColor(getColor(android.R.color.holo_red_dark));
                    binding.exportButton.setEnabled(true);
                });
            }
        }).start();
    }

    private PdfDocument generatePdf() {
        PdfDocument document = new PdfDocument();
        // Build all content sections
        float yPosition = MARGIN;
        int pageNumber = 1;
        List<DrawCommand> currentPageCommands = new ArrayList<>();

        // Title
        currentPageCommands.add(new TitleCommand(getString(R.string.pdf_report_title), MARGIN, yPosition));
        yPosition += 30;

        // Date range subtitle
        String dateRange = fromDate.format(DISPLAY_FORMAT) + " — " + toDate.format(DISPLAY_FORMAT);
        currentPageCommands.add(new SubtitleCommand(dateRange, MARGIN, yPosition));
        yPosition += 30;

        if (binding.includeHabitsCheckbox.isChecked()) {
            yPosition = addHabitSection(document, currentPageCommands, pageNumber, yPosition);
            pageNumber = document.getPages().size() + 1;
            if (!currentPageCommands.isEmpty()) {
                // continue on same page tracking
            }
        }

        if (binding.includeSleepCheckbox.isChecked()) {
            // Check if we need a new page
            if (yPosition > PAGE_HEIGHT - 300) {
                flushPage(document, currentPageCommands, pageNumber);
                pageNumber++;
                currentPageCommands = new ArrayList<>();
                yPosition = MARGIN;
            }
            yPosition = addSleepSection(document, currentPageCommands, pageNumber, yPosition);
            pageNumber = document.getPages().size() + 1;
        }

        if (binding.includeDiaryCheckbox.isChecked()) {
            if (yPosition > PAGE_HEIGHT - 300) {
                flushPage(document, currentPageCommands, pageNumber);
                pageNumber++;
                currentPageCommands = new ArrayList<>();
                yPosition = MARGIN;
            }
            yPosition = addDiarySection(document, currentPageCommands, pageNumber, yPosition);
            pageNumber = document.getPages().size() + 1;
        }

        if (binding.includeEmotionsCheckbox.isChecked()) {
            if (yPosition > PAGE_HEIGHT - 300) {
                flushPage(document, currentPageCommands, pageNumber);
                pageNumber++;
                currentPageCommands = new ArrayList<>();
                yPosition = MARGIN;
            }
            yPosition = addEmotionSection(document, currentPageCommands, pageNumber, yPosition);
            pageNumber = document.getPages().size() + 1;
        }

        // Correlation section (always included when there's data)
        if (yPosition > PAGE_HEIGHT - 200) {
            flushPage(document, currentPageCommands, pageNumber);
            pageNumber++;
            currentPageCommands = new ArrayList<>();
            yPosition = MARGIN;
        }
        yPosition = addCorrelationSection(document, currentPageCommands, pageNumber, yPosition);
        pageNumber = document.getPages().size() + 1;

        // Flush remaining commands
        if (!currentPageCommands.isEmpty()) {
            flushPage(document, currentPageCommands, pageNumber);
        }

        // Ensure at least one page exists
        if (document.getPages().size() == 0) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            document.finishPage(page);
        }

        return document;
    }

    private void flushPage(PdfDocument document, List<DrawCommand> commands, int pageNumber) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        for (DrawCommand cmd : commands) {
            cmd.draw(canvas);
        }
        document.finishPage(page);
        commands.clear();
    }

    private float addHabitSection(PdfDocument document, List<DrawCommand> commands, int pageNumber, float yPosition) {
        List<Habit> habits = MainActivity.getSharedHabits();
        if (habits == null || habits.isEmpty()) {
            commands.add(new SectionHeaderCommand(getString(R.string.pdf_section_habits), MARGIN, yPosition));
            yPosition += 25;
            commands.add(new TextCommand(getString(R.string.pdf_no_data), MARGIN, yPosition, 12f, Color.GRAY));
            yPosition += 20;
            return yPosition;
        }

        // Section header
        commands.add(new SectionHeaderCommand(getString(R.string.pdf_section_habits), MARGIN, yPosition));
        yPosition += 25;

        // Build daily habit points data
        List<String> labels = new ArrayList<>();
        List<Float> habitPoints = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
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

        // Draw chart
        commands.add(new ChartCommand(labels, habitPoints, avgPoints, MARGIN, yPosition,
                PAGE_WIDTH - 2 * MARGIN, 180, 0xFF388E3C,
                getString(R.string.stats_habit_points)));
        yPosition += 200;

        // Summary stats
        commands.add(new TextCommand(
                getString(R.string.total_points, (int) totalPoints) + "  |  " + getString(R.string.average_points, avgPoints),
                MARGIN, yPosition, 11f, Color.DKGRAY));
        yPosition += 25;

        // Per-habit breakdown table
        if (yPosition > PAGE_HEIGHT - 200) {
            flushPage(document, commands, pageNumber);
            pageNumber++;
            yPosition = MARGIN;
        }

        commands.add(new TextCommand(getString(R.string.pdf_habit_breakdown), MARGIN, yPosition, 13f, Color.BLACK));
        yPosition += 20;

        for (Habit habit : habits) {
            if (yPosition > PAGE_HEIGHT - 50) {
                flushPage(document, commands, pageNumber);
                pageNumber++;
                yPosition = MARGIN;
            }
            int habitTotal = 0;
            int habitDays = 0;
            for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
                int score = scoringService.calculateHabitScore(habit, d, d).getScore();
                habitTotal += score;
                if (score > 0) {
                    habitDays++;
                }
            }
            String line = habit.getName() + ": " + habitTotal + " pts (" + habitDays + " days)";
            commands.add(new TextCommand(line, MARGIN + 10, yPosition, 11f, Color.DKGRAY));
            yPosition += 16;
        }
        yPosition += 10;

        return yPosition;
    }

    private float addSleepSection(PdfDocument document, List<DrawCommand> commands, int pageNumber, float yPosition) {
        List<SleepEntry> allEntries = MainActivity.getSharedSleepEntries();
        if (allEntries == null) {
            allEntries = new ArrayList<>();
        }

        commands.add(new SectionHeaderCommand(getString(R.string.pdf_section_sleep), MARGIN, yPosition));
        yPosition += 25;

        Map<LocalDate, List<SleepEntry>> entriesByDate = new TreeMap<>();
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            entriesByDate.put(d, new ArrayList<>());
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(fromDate) && !entry.getDate().isAfter(toDate)) {
                List<SleepEntry> dayList = entriesByDate.get(entry.getDate());
                if (dayList != null) {
                    dayList.add(entry);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Float> durations = new ArrayList<>();
        float totalDuration = 0f;
        int daysWithData = 0;
        float minDuration = Float.MAX_VALUE;
        float maxDuration = 0f;

        for (Map.Entry<LocalDate, List<SleepEntry>> mapEntry : entriesByDate.entrySet()) {
            labels.add(mapEntry.getKey().format(LABEL_FORMAT));
            List<SleepEntry> dayEntries = mapEntry.getValue();
            float dayDuration = 0f;
            for (SleepEntry entry : dayEntries) {
                dayDuration += (float) entry.getHours();
            }
            durations.add(dayDuration);
            if (!dayEntries.isEmpty()) {
                totalDuration += dayDuration;
                daysWithData++;
                minDuration = Math.min(minDuration, dayDuration);
                maxDuration = Math.max(maxDuration, dayDuration);
            }
        }

        float avgDuration = daysWithData > 0 ? totalDuration / daysWithData : 0f;
        if (minDuration == Float.MAX_VALUE) {
            minDuration = 0f;
        }

        // Draw chart
        commands.add(new ChartCommand(labels, durations, avgDuration, MARGIN, yPosition,
                PAGE_WIDTH - 2 * MARGIN, 180, 0xFF4CAF50,
                getString(R.string.stats_sleep_duration)));
        yPosition += 200;

        // Summary
        String summary = String.format(getString(R.string.sleep_avg), avgDuration) + "  |  "
                + String.format(getString(R.string.sleep_min), minDuration) + "  |  "
                + String.format(getString(R.string.sleep_max), maxDuration);
        commands.add(new TextCommand(summary, MARGIN, yPosition, 11f, Color.DKGRAY));
        yPosition += 25;

        // List individual sleep log entries in range
        List<SleepEntry> rangeEntries = new ArrayList<>();
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(fromDate) && !entry.getDate().isAfter(toDate)) {
                rangeEntries.add(entry);
            }
        }

        if (!rangeEntries.isEmpty()) {
            if (yPosition > PAGE_HEIGHT - 100) {
                flushPage(document, commands, pageNumber);
                pageNumber++;
                yPosition = MARGIN;
            }
            commands.add(new TextCommand(getString(R.string.pdf_sleep_entries), MARGIN, yPosition, 13f, Color.BLACK));
            yPosition += 20;

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
            for (SleepEntry entry : rangeEntries) {
                if (yPosition > PAGE_HEIGHT - 40) {
                    flushPage(document, commands, pageNumber);
                    pageNumber++;
                    yPosition = MARGIN;
                }
                String line = entry.getDate().format(DISPLAY_FORMAT) + "  "
                        + entry.getFromTime().format(timeFormat) + " - "
                        + entry.getUntilTime().format(timeFormat)
                        + String.format(" (%.1f h)", entry.getHours());
                if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
                    line += " — " + entry.getNotes();
                }
                if (line.length() > 80) {
                    line = line.substring(0, 77) + "...";
                }
                commands.add(new TextCommand(line, MARGIN + 10, yPosition, 10f, Color.DKGRAY));
                yPosition += 14;
            }
        }
        yPosition += 10;

        return yPosition;
    }

    private float addDiarySection(PdfDocument document, List<DrawCommand> commands, int pageNumber, float yPosition) {
        List<DiaryEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedCurrentUser();
        if (user != null && MainActivity.getSharedDiaryEntryRepository() != null) {
            allEntries = MainActivity.getSharedDiaryEntryRepository().findByUserId(user.getId());
        }

        commands.add(new SectionHeaderCommand(getString(R.string.pdf_section_diary), MARGIN, yPosition));
        yPosition += 25;

        List<String> labels = new ArrayList<>();
        List<Float> diaryPoints = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
            int dayPoints = diaryService.getDayPoints(allEntries, d);
            diaryPoints.add((float) dayPoints);
            if (dayPoints > 0) {
                totalPoints += dayPoints;
                daysWithData++;
            }
        }
        float avgPoints = daysWithData > 0 ? totalPoints / daysWithData : 0f;

        // Draw chart
        commands.add(new ChartCommand(labels, diaryPoints, avgPoints, MARGIN, yPosition,
                PAGE_WIDTH - 2 * MARGIN, 180, 0xFF66BB6A,
                getString(R.string.stats_diary_points)));
        yPosition += 200;

        // Summary
        commands.add(new TextCommand(
                getString(R.string.total_points, (int) totalPoints) + "  |  " + getString(R.string.average_points, avgPoints),
                MARGIN, yPosition, 11f, Color.DKGRAY));
        yPosition += 25;

        // List diary entries in range
        List<DiaryEntry> rangeEntries = new ArrayList<>();
        for (DiaryEntry entry : allEntries) {
            if (!entry.getEventDate().isBefore(fromDate) && !entry.getEventDate().isAfter(toDate)) {
                rangeEntries.add(entry);
            }
        }

        if (!rangeEntries.isEmpty()) {
            if (yPosition > PAGE_HEIGHT - 100) {
                flushPage(document, commands, pageNumber);
                pageNumber++;
                yPosition = MARGIN;
            }
            commands.add(new TextCommand(getString(R.string.pdf_diary_entries), MARGIN, yPosition, 13f, Color.BLACK));
            yPosition += 20;

            for (DiaryEntry entry : rangeEntries) {
                if (yPosition > PAGE_HEIGHT - 40) {
                    flushPage(document, commands, pageNumber);
                    pageNumber++;
                    yPosition = MARGIN;
                }
                String line = entry.getEventDate().format(DISPLAY_FORMAT) + " - "
                        + entry.getDescription()
                        + " (" + entry.getSignificance().getPoints() + " pts)";
                // Truncate long descriptions
                if (line.length() > 80) {
                    line = line.substring(0, 77) + "...";
                }
                commands.add(new TextCommand(line, MARGIN + 10, yPosition, 10f, Color.DKGRAY));
                yPosition += 14;
            }
        }
        yPosition += 10;

        return yPosition;
    }

    private static final int[] PAIR_COLORS_ANDROID = {
            0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63, 0xFF9C27B0,
            0xFF00BCD4, 0xFFFF5722, 0xFF795548, 0xFF607D8B, 0xFF8BC34A
    };

    private float addEmotionSection(PdfDocument document, List<DrawCommand> commands, int pageNumber, float yPosition) {
        List<EmotionEntry> allEntries = new ArrayList<>();
        User user = MainActivity.getSharedCurrentUser();
        if (user != null && MainActivity.getSharedEmotionEntryRepository() != null) {
            allEntries = MainActivity.getSharedEmotionEntryRepository().findByUserId(user.getId());
        }

        commands.add(new SectionHeaderCommand(getString(R.string.pdf_section_emotions), MARGIN, yPosition));
        yPosition += 25;

        List<EmotionEntry> rangeEntries = new ArrayList<>();
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (!entryDate.isBefore(fromDate) && !entryDate.isAfter(toDate)) {
                rangeEntries.add(entry);
            }
        }

        if (rangeEntries.isEmpty()) {
            commands.add(new TextCommand(getString(R.string.pdf_no_data), MARGIN, yPosition, 12f, Color.GRAY));
            yPosition += 20;
            return yPosition;
        }

        // Build date labels
        List<String> labels = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
            dates.add(d);
        }

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

        // For each pair, compute daily average strength
        List<String> pairIds = new ArrayList<>(entriesByPair.keySet());
        List<List<Float>> pairDailyValues = new ArrayList<>();
        for (String pairId : pairIds) {
            List<EmotionEntry> pairEntries = entriesByPair.get(pairId);
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

        // Draw emotion line chart
        int legendLines = (pairIds.size() + 2) / 3;
        float legendHeight = legendLines * 14f + 10f;
        commands.add(new EmotionLineChartCommand(labels, pairIds, pairLabelMap, pairDailyValues,
                MARGIN, yPosition, PAGE_WIDTH - 2 * MARGIN, 200,
                getString(R.string.pdf_section_emotions)));
        yPosition += 220 + legendHeight;

        // Summary
        commands.add(new TextCommand(
                String.format("Total: %d entries across %d emotion pairs", rangeEntries.size(), pairIds.size()),
                MARGIN, yPosition, 11f, Color.DKGRAY));
        yPosition += 25;

        // Emotion entries list
        if (yPosition > PAGE_HEIGHT - 100) {
            flushPage(document, commands, pageNumber);
            pageNumber++;
            yPosition = MARGIN;
        }
        commands.add(new TextCommand(getString(R.string.pdf_emotion_entries), MARGIN, yPosition, 13f, Color.BLACK));
        yPosition += 20;

        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (EmotionEntry entry : rangeEntries) {
            if (yPosition > PAGE_HEIGHT - 40) {
                flushPage(document, commands, pageNumber);
                pageNumber++;
                yPosition = MARGIN;
            }
            String pairText = entry.getEmotionPair() != null ? entry.getEmotionPair().toString() : "";
            String line = entry.getRecordedAt().format(dtFmt) + "  " + pairText
                    + " [" + entry.getStrength() + "]";
            if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
                line += " — " + entry.getNotes();
            }
            if (line.length() > 80) {
                line = line.substring(0, 77) + "...";
            }
            commands.add(new TextCommand(line, MARGIN + 10, yPosition, 10f, Color.DKGRAY));
            yPosition += 14;
        }
        yPosition += 10;

        return yPosition;
    }

    private float addCorrelationSection(PdfDocument document, List<DrawCommand> commands, int pageNumber, float yPosition) {
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

        List<EventCorrelation> correlations = correlationService.calculateCorrelations(
                habits, diaryEntries, sleepEntries);

        commands.add(new SectionHeaderCommand(getString(R.string.pdf_section_correlations), MARGIN, yPosition));
        yPosition += 25;

        if (correlations.isEmpty()) {
            commands.add(new TextCommand(getString(R.string.stats_no_correlations), MARGIN, yPosition, 12f, Color.GRAY));
            yPosition += 20;
            return yPosition;
        }

        commands.add(new TextCommand(getString(R.string.pdf_correlation_description),
                MARGIN, yPosition, 10f, Color.DKGRAY));
        yPosition += 18;

        for (EventCorrelation corr : correlations) {
            if (yPosition > PAGE_HEIGHT - 40) {
                flushPage(document, commands, pageNumber);
                pageNumber++;
                yPosition = MARGIN;
            }
            String line = corr.getEventA() + " \u2194 " + corr.getEventB()
                    + "  " + String.format("%+.3f", corr.getCorrelation())
                    + "  (" + corr.getSharedDays() + " days)";
            if (line.length() > 85) {
                line = line.substring(0, 82) + "...";
            }
            commands.add(new TextCommand(line, MARGIN + 10, yPosition, 10f, Color.DKGRAY));
            yPosition += 14;
        }
        yPosition += 10;

        return yPosition;
    }

    // Draw command interface and implementations for deferred rendering
    private interface DrawCommand {
        void draw(Canvas canvas);
    }

    private static class TitleCommand implements DrawCommand {
        private final String text;
        private final float x;
        private final float y;

        TitleCommand(String text, float x, float y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setTextSize(20f);
            paint.setFakeBoldText(true);
            canvas.drawText(text, x, y + 20f, paint);
        }
    }

    private static class SubtitleCommand implements DrawCommand {
        private final String text;
        private final float x;
        private final float y;

        SubtitleCommand(String text, float x, float y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.GRAY);
            paint.setTextSize(14f);
            canvas.drawText(text, x, y + 14f, paint);
        }
    }

    private static class SectionHeaderCommand implements DrawCommand {
        private final String text;
        private final float x;
        private final float y;

        SectionHeaderCommand(String text, float x, float y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(0xFF1B5E20);
            paint.setTextSize(16f);
            paint.setFakeBoldText(true);
            canvas.drawText(text, x, y + 16f, paint);

            Paint linePaint = new Paint();
            linePaint.setColor(0xFF4CAF50);
            linePaint.setStrokeWidth(2f);
            canvas.drawLine(x, y + 20f, x + 200, y + 20f, linePaint);
        }
    }

    private static class TextCommand implements DrawCommand {
        private final String text;
        private final float x;
        private final float y;
        private final float size;
        private final int color;

        TextCommand(String text, float x, float y, float size, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);
            paint.setTextSize(size);
            canvas.drawText(text, x, y + size, paint);
        }
    }

    private static class ChartCommand implements DrawCommand {
        private final List<String> labels;
        private final List<Float> values;
        private final float average;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final int barColor;
        private final String title;

        ChartCommand(List<String> labels, List<Float> values, float average,
                     float x, float y, float width, float height, int barColor, String title) {
            this.labels = labels;
            this.values = values;
            this.average = average;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.barColor = barColor;
            this.title = title;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(Color.BLACK);
            titlePaint.setTextSize(12f);
            titlePaint.setFakeBoldText(true);
            canvas.drawText(title, x, y + 12f, titlePaint);

            float chartTop = y + 20f;
            float chartLeft = x + 30f;
            float chartRight = x + width;
            float chartBottom = y + height - 20f;
            float chartWidth = chartRight - chartLeft;
            float chartHeight = chartBottom - chartTop;

            // Axes
            Paint axisPaint = new Paint();
            axisPaint.setColor(Color.DKGRAY);
            axisPaint.setStrokeWidth(1f);
            canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint);
            canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

            if (values.isEmpty()) {
                return;
            }

            float maxValue = 0;
            for (float v : values) {
                maxValue = Math.max(maxValue, v);
            }
            if (average > maxValue) {
                maxValue = average;
            }
            maxValue = Math.max(maxValue * 1.15f, 1f);

            // Grid lines
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.LTGRAY);
            gridPaint.setStrokeWidth(0.5f);
            Paint gridLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            gridLabelPaint.setColor(Color.GRAY);
            gridLabelPaint.setTextSize(8f);
            gridLabelPaint.setTextAlign(Paint.Align.RIGHT);

            for (int i = 0; i <= 4; i++) {
                float gridY = chartBottom - (chartHeight * i / 4f);
                canvas.drawLine(chartLeft, gridY, chartRight, gridY, gridPaint);
                float val = maxValue * i / 4f;
                canvas.drawText(String.format("%.0f", val), chartLeft - 4, gridY + 3, gridLabelPaint);
            }

            // Bars
            int count = values.size();
            float barSpacing = chartWidth / count;
            float barWidth = Math.max(barSpacing * 0.65f, 1f);
            Paint bPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bPaint.setColor(barColor);
            bPaint.setStyle(Paint.Style.FILL);

            for (int i = 0; i < count; i++) {
                float val = values.get(i);
                float barHeight = (val / maxValue) * chartHeight;
                float centerX = chartLeft + barSpacing * i + barSpacing / 2;
                float left = centerX - barWidth / 2;
                float right = centerX + barWidth / 2;
                float top = chartBottom - barHeight;
                RectF rect = new RectF(left, top, right, chartBottom);
                canvas.drawRoundRect(rect, 2f, 2f, bPaint);
            }

            // Average line
            if (average > 0) {
                Paint avgPaint = new Paint();
                avgPaint.setColor(0xFFD32F2F);
                avgPaint.setStrokeWidth(1.5f);
                avgPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{6f, 4f}, 0));
                float avgY = chartBottom - (average / maxValue) * chartHeight;
                canvas.drawLine(chartLeft, avgY, chartRight, avgY, avgPaint);

                Paint avgLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                avgLabelPaint.setColor(0xFFD32F2F);
                avgLabelPaint.setTextSize(8f);
                avgLabelPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.format("Avg: %.1f", average), chartRight, avgY - 3, avgLabelPaint);
            }

            // X-axis labels (show subset to avoid overlap)
            Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            labelPaint.setColor(Color.DKGRAY);
            labelPaint.setTextSize(7f);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            int labelStep = Math.max(1, count / 10);
            for (int i = 0; i < count; i += labelStep) {
                float centerX = chartLeft + barSpacing * i + barSpacing / 2;
                if (i < labels.size()) {
                    canvas.save();
                    canvas.rotate(-45, centerX, chartBottom + 6);
                    canvas.drawText(labels.get(i), centerX, chartBottom + 14, labelPaint);
                    canvas.restore();
                }
            }
        }
    }

    private static class EmotionLineChartCommand implements DrawCommand {
        private final List<String> labels;
        private final List<String> pairIds;
        private final Map<String, String> pairLabelMap;
        private final List<List<Float>> pairDailyValues;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final String title;

        EmotionLineChartCommand(List<String> labels, List<String> pairIds,
                                 Map<String, String> pairLabelMap, List<List<Float>> pairDailyValues,
                                 float x, float y, float width, float height, String title) {
            this.labels = labels;
            this.pairIds = pairIds;
            this.pairLabelMap = pairLabelMap;
            this.pairDailyValues = pairDailyValues;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.title = title;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(Color.BLACK);
            titlePaint.setTextSize(12f);
            titlePaint.setFakeBoldText(true);
            canvas.drawText(title, x, y + 12f, titlePaint);

            float chartTop = y + 20f;
            float chartLeft = x + 30f;
            float chartRight = x + width;
            float chartBottom = y + height - 20f;
            float chartWidth = chartRight - chartLeft;
            float chartHeight = chartBottom - chartTop;
            float yCenter = chartTop + chartHeight / 2f;

            // Axes
            Paint axisPaint = new Paint();
            axisPaint.setColor(Color.DKGRAY);
            axisPaint.setStrokeWidth(1f);
            canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint);
            canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

            // Grid lines at -10, -5, 0, 5, 10
            Paint gridPaint = new Paint();
            gridPaint.setStrokeWidth(0.5f);
            Paint gridLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            gridLabelPaint.setColor(Color.GRAY);
            gridLabelPaint.setTextSize(8f);
            gridLabelPaint.setTextAlign(Paint.Align.RIGHT);

            int[] gridValues = {-10, -5, 0, 5, 10};
            for (int val : gridValues) {
                float gridY = yCenter - (val / 10f) * (chartHeight / 2f);
                if (val == 0) {
                    gridPaint.setColor(Color.GRAY);
                    gridPaint.setStrokeWidth(0.8f);
                } else {
                    gridPaint.setColor(Color.LTGRAY);
                    gridPaint.setStrokeWidth(0.5f);
                }
                canvas.drawLine(chartLeft, gridY, chartRight, gridY, gridPaint);
                canvas.drawText(String.valueOf(val), chartLeft - 4, gridY + 3, gridLabelPaint);
            }

            if (labels.isEmpty()) {
                return;
            }

            int count = labels.size();
            float pointSpacing = count > 1 ? chartWidth / (count - 1) : chartWidth;

            // Draw lines and dots for each emotion pair
            for (int p = 0; p < pairIds.size(); p++) {
                int lineColor = PAIR_COLORS_ANDROID[p % PAIR_COLORS_ANDROID.length];
                List<Float> dailyValues = pairDailyValues.get(p);

                Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                linePaint.setColor(lineColor);
                linePaint.setStrokeWidth(1.5f);
                linePaint.setStyle(Paint.Style.STROKE);

                Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                dotPaint.setColor(lineColor);
                dotPaint.setStyle(Paint.Style.FILL);

                // Draw connecting lines
                Float prevVal = null;
                float prevX = 0;
                for (int i = 0; i < count; i++) {
                    Float val = dailyValues.get(i);
                    if (val == null) {
                        prevVal = null;
                        continue;
                    }
                    float px = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartWidth / 2f;
                    float py = yCenter - (val / 10f) * (chartHeight / 2f);
                    if (prevVal != null) {
                        float prevY = yCenter - (prevVal / 10f) * (chartHeight / 2f);
                        canvas.drawLine(prevX, prevY, px, py, linePaint);
                    }
                    prevVal = val;
                    prevX = px;
                }

                // Draw dots
                for (int i = 0; i < count; i++) {
                    Float val = dailyValues.get(i);
                    if (val == null) {
                        continue;
                    }
                    float px = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartWidth / 2f;
                    float py = yCenter - (val / 10f) * (chartHeight / 2f);
                    canvas.drawCircle(px, py, 2.5f, dotPaint);
                }
            }

            // X-axis labels
            Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            labelPaint.setColor(Color.DKGRAY);
            labelPaint.setTextSize(7f);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            int labelStep = Math.max(1, count / 10);
            for (int i = 0; i < count; i += labelStep) {
                float centerX = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartWidth / 2f;
                if (i < labels.size()) {
                    canvas.save();
                    canvas.rotate(-45, centerX, chartBottom + 6);
                    canvas.drawText(labels.get(i), centerX, chartBottom + 14, labelPaint);
                    canvas.restore();
                }
            }

            // Legend below chart
            float legendY = chartBottom + 25;
            float colWidth = chartWidth / 3f;
            Paint legendBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            legendBoxPaint.setStyle(Paint.Style.FILL);
            Paint legendTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            legendTextPaint.setColor(Color.DKGRAY);
            legendTextPaint.setTextSize(7f);

            for (int p = 0; p < pairIds.size(); p++) {
                int lineColor = PAIR_COLORS_ANDROID[p % PAIR_COLORS_ANDROID.length];
                int col = p % 3;
                int row = p / 3;
                float lx = chartLeft + col * colWidth;
                float ly = legendY + row * 14f;

                legendBoxPaint.setColor(lineColor);
                canvas.drawRect(lx, ly - 3, lx + 10, ly + 3, legendBoxPaint);

                String label = pairLabelMap.get(pairIds.get(p));
                if (label != null && label.length() > 30) {
                    label = label.substring(0, 27) + "...";
                }
                canvas.drawText(label != null ? label : "", lx + 13, ly + 3, legendTextPaint);
            }
        }
    }
}
