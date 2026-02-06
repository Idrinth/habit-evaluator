package de.idrinth.habitevaluator.desktop.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionStrengthFormatter;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PdfExportController {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0x1B, 0x5E, 0x20));
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font TABLE_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private CheckBox includeHabitsCheckbox;

    @FXML
    private CheckBox includeSleepCheckbox;

    @FXML
    private CheckBox includeDiaryCheckbox;

    @FXML
    private CheckBox includeEmotionsCheckbox;

    @FXML
    private Label statusLabel;

    private HabitRepository habitRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private SleepEntryRepository sleepEntryRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private User currentUser;
    private final HabitScoringService scoringService = new HabitScoringService();
    private final DiaryService diaryService = new DiaryService();
    private final EventCorrelationService correlationService = new EventCorrelationService();

    @FXML
    public void initialize() {
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusDays(29));
    }

    public void setHabitRepository(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public void setDiaryEntryRepository(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    public void setSleepEntryRepository(SleepEntryRepository sleepEntryRepository) {
        this.sleepEntryRepository = sleepEntryRepository;
    }

    public void setEmotionEntryRepository(EmotionEntryRepository emotionEntryRepository) {
        this.emotionEntryRepository = emotionEntryRepository;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    private void handleExport() {
        if (!includeHabitsCheckbox.isSelected()
                && !includeSleepCheckbox.isSelected()
                && !includeDiaryCheckbox.isSelected()
                && !includeEmotionsCheckbox.isSelected()) {
            statusLabel.setText("Select at least one section.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        if (fromDate == null || toDate == null) {
            statusLabel.setText("Select both dates.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (fromDate.isAfter(toDate)) {
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
            fromDatePicker.setValue(fromDate);
            toDatePicker.setValue(toDate);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        String defaultName = "habit_report_" + fromDate.format(DISPLAY_FORMAT) + "_to_" + toDate.format(DISPLAY_FORMAT) + ".pdf";
        fileChooser.setInitialFileName(defaultName);

        Stage stage = (Stage) statusLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        statusLabel.setText("Generating PDF...");
        statusLabel.setStyle("-fx-text-fill: gray;");

        LocalDate finalFromDate = fromDate;
        LocalDate finalToDate = toDate;
        new Thread(() -> {
            try {
                generatePdf(file, finalFromDate, finalToDate,
                        includeHabitsCheckbox.isSelected(),
                        includeSleepCheckbox.isSelected(),
                        includeDiaryCheckbox.isSelected(),
                        includeEmotionsCheckbox.isSelected());
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("PDF exported successfully.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Export failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private void generatePdf(File file, LocalDate fromDate, LocalDate toDate,
                              boolean includeHabits, boolean includeSleep, boolean includeDiary,
                              boolean includeEmotions)
            throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Title
        Paragraph title = new Paragraph("Habit Evaluator Report", TITLE_FONT);
        document.add(title);

        // Date range
        String dateRange = fromDate.format(DISPLAY_FORMAT) + " \u2014 " + toDate.format(DISPLAY_FORMAT);
        Paragraph subtitle = new Paragraph(dateRange, SUBTITLE_FONT);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        String userId = currentUser != null ? currentUser.getId() : null;

        if (includeHabits) {
            addHabitSection(document, writer, userId, fromDate, toDate);
        }
        if (includeSleep) {
            addSleepSection(document, writer, userId, fromDate, toDate);
        }
        if (includeDiary) {
            addDiarySection(document, writer, userId, fromDate, toDate);
        }
        if (includeEmotions) {
            addEmotionSection(document, writer, userId, fromDate, toDate);
        }

        addCorrelationSection(document, userId);

        document.close();
    }

    private void addHabitSection(Document document, PdfWriter writer, String userId,
                                  LocalDate fromDate, LocalDate toDate) throws DocumentException {
        List<Habit> habitList = userId != null ? habitRepository.findByUserId(userId) : habitRepository.findAll();

        Paragraph header = new Paragraph("Habits", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);

        Paragraph habitIntro = new Paragraph(
                "Daily habit scores reflect how consistently you performed your tracked habits. Higher scores indicate greater adherence to your routines.",
                SMALL_FONT);
        habitIntro.setSpacingAfter(10);
        document.add(habitIntro);

        if (habitList.isEmpty()) {
            document.add(new Paragraph("No habit data available.", BODY_FONT));
            return;
        }

        List<String> labels = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        float totalPoints = 0f;
        int daysWithData = 0;

        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
            int dayTotal = 0;
            for (Habit habit : habitList) {
                dayTotal += scoringService.calculateHabitScore(habit, d, d).getScore();
            }
            values.add((float) dayTotal);
            if (dayTotal > 0) {
                totalPoints += dayTotal;
                daysWithData++;
            }
        }
        float avgPoints = daysWithData > 0 ? totalPoints / daysWithData : 0f;

        drawBarChart(writer, document, labels, values, avgPoints, new Color(0x38, 0x8E, 0x3C), "Habit Points");

        Paragraph stats = new Paragraph(
                String.format("Total: %d pts  |  Average: %.1f pts/day", (int) totalPoints, avgPoints),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        Paragraph breakdownTitle = new Paragraph("Per-Habit Breakdown",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
        breakdownTitle.setSpacingBefore(5);
        breakdownTitle.setSpacingAfter(5);
        document.add(breakdownTitle);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1, 1});

        addTableHeader(table, "Habit");
        addTableHeader(table, "Points");
        addTableHeader(table, "Active Days");

        for (Habit habit : habitList) {
            int habitTotal = 0;
            int habitDays = 0;
            for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
                int score = scoringService.calculateHabitScore(habit, d, d).getScore();
                habitTotal += score;
                if (score > 0) {
                    habitDays++;
                }
            }
            addTableCell(table, habit.getName());
            addTableCell(table, String.valueOf(habitTotal));
            addTableCell(table, String.valueOf(habitDays));
        }

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void addSleepSection(Document document, PdfWriter writer, String userId,
                                  LocalDate fromDate, LocalDate toDate) throws DocumentException {
        List<SleepEntry> allEntries = new ArrayList<>();
        if (sleepEntryRepository != null && userId != null) {
            allEntries = sleepEntryRepository.findByUserId(userId);
        }

        Paragraph header = new Paragraph("Sleep", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);

        Paragraph sleepIntro = new Paragraph(
                "This section summarizes your recorded sleep patterns including duration, timing, and consistency across the selected period.",
                SMALL_FONT);
        sleepIntro.setSpacingAfter(10);
        document.add(sleepIntro);

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

        if (daysWithData == 0) {
            document.add(new Paragraph("No sleep data available.", BODY_FONT));
            return;
        }

        drawBarChart(writer, document, labels, durations, avgDuration, new Color(0x4C, 0xAF, 0x50), "Sleep Duration (hours)");

        Paragraph stats = new Paragraph(
                String.format("Avg: %.1f h  |  Min: %.1f h  |  Max: %.1f h", avgDuration, minDuration, maxDuration),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        List<SleepEntry> rangeEntries = new ArrayList<>();
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(fromDate) && !entry.getDate().isAfter(toDate)) {
                rangeEntries.add(entry);
            }
        }

        if (!rangeEntries.isEmpty()) {
            Paragraph entriesTitle = new Paragraph("Sleep Entries",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
            entriesTitle.setSpacingBefore(5);
            entriesTitle.setSpacingAfter(5);
            document.add(entriesTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 1.5f, 1.5f, 1});

            addTableHeader(table, "Date");
            addTableHeader(table, "From");
            addTableHeader(table, "Until");
            addTableHeader(table, "Hours");

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
            for (SleepEntry entry : rangeEntries) {
                addTableCell(table, entry.getDate().format(DISPLAY_FORMAT));
                addTableCell(table, entry.getFromTime().format(timeFormat));
                addTableCell(table, entry.getUntilTime().format(timeFormat));
                addTableCell(table, String.format("%.1f", entry.getHours()));
            }

            table.setSpacingAfter(15);
            document.add(table);
        }
    }

    private void addDiarySection(Document document, PdfWriter writer, String userId,
                                  LocalDate fromDate, LocalDate toDate) throws DocumentException {
        List<DiaryEntry> allEntries = new ArrayList<>();
        if (diaryEntryRepository != null && userId != null) {
            allEntries = diaryEntryRepository.findByUserId(userId);
        }

        Paragraph header = new Paragraph("Diary", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);

        Paragraph diaryIntro = new Paragraph(
                "Diary entries capture significant events and their perceived impact. Points are assigned based on the significance level you gave each entry.",
                SMALL_FONT);
        diaryIntro.setSpacingAfter(10);
        document.add(diaryIntro);

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

        if (daysWithData == 0 && allEntries.stream().noneMatch(
                e -> !e.getEventDate().isBefore(fromDate) && !e.getEventDate().isAfter(toDate))) {
            document.add(new Paragraph("No diary data available.", BODY_FONT));
            return;
        }

        drawBarChart(writer, document, labels, diaryPoints, avgPoints, new Color(0x66, 0xBB, 0x6A), "Diary Points");

        Paragraph stats = new Paragraph(
                String.format("Total: %d pts  |  Average: %.1f pts/day", (int) totalPoints, avgPoints),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        List<DiaryEntry> rangeEntries = diaryService.getEntriesInRange(allEntries, fromDate, toDate);

        if (!rangeEntries.isEmpty()) {
            Paragraph entriesTitle = new Paragraph("Diary Entries",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
            entriesTitle.setSpacingBefore(5);
            entriesTitle.setSpacingAfter(5);
            document.add(entriesTitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4, 1});

            addTableHeader(table, "Date");
            addTableHeader(table, "Description");
            addTableHeader(table, "Points");

            for (DiaryEntry entry : rangeEntries) {
                addTableCell(table, entry.getEventDate().format(DISPLAY_FORMAT));
                String desc = entry.getDescription();
                if (desc != null && desc.length() > 60) {
                    desc = desc.substring(0, 57) + "...";
                }
                addTableCell(table, desc != null ? desc : "");
                addTableCell(table, String.valueOf(entry.getSignificance().getPoints()));
            }

            table.setSpacingAfter(15);
            document.add(table);
        }
    }

    private static final Color[] PAIR_COLORS = {
            new Color(0x4C, 0xAF, 0x50), new Color(0x21, 0x96, 0xF3), new Color(0xFF, 0x98, 0x00),
            new Color(0xE9, 0x1E, 0x63), new Color(0x9C, 0x27, 0xB0), new Color(0x00, 0xBC, 0xD4),
            new Color(0xFF, 0x57, 0x22), new Color(0x79, 0x55, 0x48), new Color(0x60, 0x7D, 0x8B),
            new Color(0x8B, 0xC3, 0x4A)
    };

    private void addEmotionSection(Document document, PdfWriter writer, String userId,
                                     LocalDate fromDate, LocalDate toDate) throws DocumentException {
        List<EmotionEntry> allEntries = new ArrayList<>();
        if (emotionEntryRepository != null && userId != null) {
            allEntries = emotionEntryRepository.findByUserId(userId);
        }

        Paragraph header = new Paragraph("Emotions", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);

        Paragraph emotionIntro = new Paragraph(
                "Emotion entries track your self-reported emotional states on paired scales over time. Daily averages are shown per emotion pair.",
                SMALL_FONT);
        emotionIntro.setSpacingAfter(10);
        document.add(emotionIntro);

        List<EmotionEntry> rangeEntries = new ArrayList<>();
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (!entryDate.isBefore(fromDate) && !entryDate.isAfter(toDate)) {
                rangeEntries.add(entry);
            }
        }

        if (rangeEntries.isEmpty()) {
            document.add(new Paragraph("No emotion data available.", BODY_FONT));
            return;
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

        drawEmotionLineChart(writer, document, labels, pairIds, pairLabelMap, pairDailyValues);

        Paragraph stats = new Paragraph(
                String.format("Total: %d entries across %d emotion pairs", rangeEntries.size(), pairIds.size()),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        // Entries table
        Paragraph entriesTitle = new Paragraph("Emotion Entries",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
        entriesTitle.setSpacingBefore(5);
        entriesTitle.setSpacingAfter(5);
        document.add(entriesTitle);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2.5f, 1, 3});

        addTableHeader(table, "Date/Time");
        addTableHeader(table, "Emotion Pair");
        addTableHeader(table, "Strength");
        addTableHeader(table, "Notes");

        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (EmotionEntry entry : rangeEntries) {
            addTableCell(table, entry.getRecordedAt().format(dtFmt));
            String pairText = entry.getEmotionPair() != null ? entry.getEmotionPair().toString() : "";
            addTableCell(table, pairText);
            addTableCell(table, EmotionStrengthFormatter.format(entry.getStrength(), entry.getEmotionPair()));
            String notes = entry.getNotes();
            if (notes != null && notes.length() > 50) {
                notes = notes.substring(0, 47) + "...";
            }
            addTableCell(table, notes != null ? notes : "");
        }

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void drawEmotionLineChart(PdfWriter writer, Document document, List<String> labels,
                                       List<String> pairIds, Map<String, String> pairLabelMap,
                                       List<List<Float>> pairDailyValues) throws DocumentException {
        float chartWidth = PageSize.A4.getWidth() - 80;
        float chartHeight = 180f;
        int legendLines = (pairIds.size() + 2) / 3;
        float legendHeight = legendLines * 14f + 10f;
        float totalHeight = chartHeight + 40 + legendHeight;

        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(totalHeight);
        document.add(spacer);

        PdfContentByte cb = writer.getDirectContent();
        float pageHeight = document.getPageSize().getHeight();
        float xStart = document.leftMargin();
        float yBottom = pageHeight - document.topMargin() - writer.getVerticalPosition(false) + totalHeight - chartHeight - 15;
        float yTop = yBottom + chartHeight;

        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // Chart title
            cb.beginText();
            cb.setFontAndSize(bfBold, 11);
            cb.setColorFill(Color.BLACK);
            cb.showTextAligned(Element.ALIGN_LEFT, "Emotion Pairs", xStart, yTop + 15, 0);
            cb.endText();

            float chartLeft = xStart + 30;
            float chartRight = xStart + chartWidth;
            float chartAreaWidth = chartRight - chartLeft;
            float yCenter = yBottom + chartHeight / 2f;

            // Axes
            cb.setColorStroke(Color.DARK_GRAY);
            cb.setLineWidth(1f);
            cb.moveTo(chartLeft, yBottom);
            cb.lineTo(chartLeft, yTop);
            cb.stroke();
            cb.moveTo(chartLeft, yBottom);
            cb.lineTo(chartRight, yBottom);
            cb.stroke();

            // Grid lines at -10, -5, 0, 5, 10
            int[] gridValues = {-10, -5, 0, 5, 10};
            for (int val : gridValues) {
                float gridY = yCenter + (val / 10f) * (chartHeight / 2f);
                if (val == 0) {
                    cb.setColorStroke(Color.GRAY);
                    cb.setLineWidth(0.8f);
                } else {
                    cb.setColorStroke(Color.LIGHT_GRAY);
                    cb.setLineWidth(0.5f);
                }
                cb.moveTo(chartLeft, gridY);
                cb.lineTo(chartRight, gridY);
                cb.stroke();

                cb.beginText();
                cb.setFontAndSize(bf, 7);
                cb.setColorFill(Color.GRAY);
                cb.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(val), chartLeft - 4, gridY - 3, 0);
                cb.endText();
            }

            if (labels.isEmpty()) {
                return;
            }

            int count = labels.size();
            float pointSpacing = count > 1 ? chartAreaWidth / (count - 1) : chartAreaWidth;

            // Draw lines and dots for each emotion pair
            for (int p = 0; p < pairIds.size(); p++) {
                Color lineColor = PAIR_COLORS[p % PAIR_COLORS.length];
                List<Float> dailyValues = pairDailyValues.get(p);

                cb.setColorStroke(lineColor);
                cb.setLineWidth(1.5f);
                cb.setLineDash(0);

                Float prevVal = null;
                float prevX = 0;
                for (int i = 0; i < count; i++) {
                    Float val = dailyValues.get(i);
                    if (val == null) {
                        prevVal = null;
                        continue;
                    }
                    float x = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartAreaWidth / 2f;
                    float y = yCenter + (val / 10f) * (chartHeight / 2f);
                    if (prevVal != null) {
                        cb.moveTo(prevX, yCenter + (prevVal / 10f) * (chartHeight / 2f));
                        cb.lineTo(x, y);
                        cb.stroke();
                    }
                    prevVal = val;
                    prevX = x;
                }

                // Draw dots
                cb.setColorFill(lineColor);
                for (int i = 0; i < count; i++) {
                    Float val = dailyValues.get(i);
                    if (val == null) {
                        continue;
                    }
                    float x = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartAreaWidth / 2f;
                    float y = yCenter + (val / 10f) * (chartHeight / 2f);
                    cb.circle(x, y, 2.5f);
                    cb.fill();
                }
            }

            // X-axis labels
            int labelStep = Math.max(1, count / 10);
            for (int i = 0; i < count; i += labelStep) {
                float centerX = count > 1 ? chartLeft + pointSpacing * i : chartLeft + chartAreaWidth / 2f;
                if (i < labels.size()) {
                    cb.beginText();
                    cb.setFontAndSize(bf, 7);
                    cb.setColorFill(Color.DARK_GRAY);
                    cb.showTextAligned(Element.ALIGN_CENTER, labels.get(i), centerX, yBottom - 10, 0);
                    cb.endText();
                }
            }

            // Legend below chart
            float legendY = yBottom - 25;
            float legendX = chartLeft;
            float colWidth = chartAreaWidth / 3f;
            for (int p = 0; p < pairIds.size(); p++) {
                Color lineColor = PAIR_COLORS[p % PAIR_COLORS.length];
                int col = p % 3;
                int row = p / 3;
                float lx = legendX + col * colWidth;
                float ly = legendY - row * 14f;

                cb.setColorFill(lineColor);
                cb.rectangle(lx, ly - 3, 10, 6);
                cb.fill();

                cb.beginText();
                cb.setFontAndSize(bf, 7);
                cb.setColorFill(Color.DARK_GRAY);
                String label = pairLabelMap.get(pairIds.get(p));
                if (label != null && label.length() > 30) {
                    label = label.substring(0, 27) + "...";
                }
                cb.showTextAligned(Element.ALIGN_LEFT, label != null ? label : "", lx + 13, ly - 3, 0);
                cb.endText();
            }
        } catch (IOException e) {
            throw new DocumentException(e);
        }
    }

    private void addCorrelationSection(Document document, String userId) throws DocumentException {
        List<Habit> habits = userId != null ? habitRepository.findByUserId(userId) : habitRepository.findAll();
        List<DiaryEntry> diaryEntries = new ArrayList<>();
        List<SleepEntry> sleepEntries = new ArrayList<>();
        List<EmotionEntry> emotionEntries = new ArrayList<>();
        if (diaryEntryRepository != null && userId != null) {
            diaryEntries = diaryEntryRepository.findByUserId(userId);
        }
        if (sleepEntryRepository != null && userId != null) {
            sleepEntries = sleepEntryRepository.findByUserId(userId);
        }
        if (emotionEntryRepository != null && userId != null) {
            emotionEntries = emotionEntryRepository.findByUserId(userId);
        }

        List<EventCorrelation> correlations = correlationService.calculateCorrelations(
                habits, diaryEntries, sleepEntries, emotionEntries);

        Paragraph header = new Paragraph("Event Correlations", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(10);
        document.add(header);

        if (correlations.isEmpty()) {
            document.add(new Paragraph("Not enough data for correlations.", BODY_FONT));
            return;
        }

        Paragraph description = new Paragraph(
                "Time-weighted Pearson correlations over the past year. Stronger absolute values indicate stronger relationships. Note: correlation does not imply causation.",
                SMALL_FONT);
        description.setSpacingAfter(10);
        document.add(description);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 3, 1.5f, 1.2f, 1.3f});

        addTableHeader(table, "Event A");
        addTableHeader(table, "Event B");
        addTableHeader(table, "Correlation");
        addTableHeader(table, "Shared Days");
        addTableHeader(table, "Confidence");

        for (EventCorrelation corr : correlations) {
            addTableCell(table, corr.getEventA());
            addTableCell(table, corr.getEventB());
            addTableCell(table, String.format("%+.3f", corr.getCorrelation()));
            addTableCell(table, String.valueOf(corr.getSharedDays()));
            double abs = Math.abs(corr.getCorrelation());
            if (abs >= 0.5) {
                addTableCell(table, "Strong");
            } else if (abs >= 0.3) {
                addTableCell(table, "Moderate");
            } else {
                addTableCell(table, "Weak");
            }
        }

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void drawBarChart(PdfWriter writer, Document document, List<String> labels,
                               List<Float> values, float average, Color barColor, String chartTitle) throws DocumentException {
        float chartWidth = PageSize.A4.getWidth() - 80;
        float chartHeight = 160f;
        float totalHeight = chartHeight + 40;

        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(totalHeight);
        document.add(spacer);

        PdfContentByte cb = writer.getDirectContent();
        float pageHeight = document.getPageSize().getHeight();
        float xStart = document.leftMargin();
        float yBottom = pageHeight - document.topMargin() - writer.getVerticalPosition(false) + totalHeight - chartHeight - 15;
        float yTop = yBottom + chartHeight;

        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // Chart title
            cb.beginText();
            cb.setFontAndSize(bfBold, 11);
            cb.setColorFill(Color.BLACK);
            cb.showTextAligned(Element.ALIGN_LEFT, chartTitle, xStart, yTop + 15, 0);
            cb.endText();

            float chartLeft = xStart + 30;
            float chartRight = xStart + chartWidth;
            float chartAreaWidth = chartRight - chartLeft;

            // Axes
            cb.setColorStroke(Color.DARK_GRAY);
            cb.setLineWidth(1f);
            cb.moveTo(chartLeft, yBottom);
            cb.lineTo(chartLeft, yTop);
            cb.stroke();
            cb.moveTo(chartLeft, yBottom);
            cb.lineTo(chartRight, yBottom);
            cb.stroke();

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
            cb.setColorStroke(Color.LIGHT_GRAY);
            cb.setLineWidth(0.5f);
            for (int i = 1; i <= 4; i++) {
                float gridY = yBottom + (chartHeight * i / 4f);
                cb.moveTo(chartLeft, gridY);
                cb.lineTo(chartRight, gridY);
                cb.stroke();

                float val = maxValue * i / 4f;
                cb.beginText();
                cb.setFontAndSize(bf, 7);
                cb.setColorFill(Color.GRAY);
                cb.showTextAligned(Element.ALIGN_RIGHT, String.format("%.0f", val), chartLeft - 4, gridY - 3, 0);
                cb.endText();
            }

            // Bars
            int count = values.size();
            float barSpacing = chartAreaWidth / count;
            float barWidth = Math.max(barSpacing * 0.65f, 1f);

            for (int i = 0; i < count; i++) {
                float val = values.get(i);
                float barHeight = (val / maxValue) * chartHeight;
                float centerX = chartLeft + barSpacing * i + barSpacing / 2;
                float left = centerX - barWidth / 2;
                float right = centerX + barWidth / 2;

                cb.setColorFill(barColor);
                cb.rectangle(left, yBottom, right - left, barHeight);
                cb.fill();
            }

            // Average line
            if (average > 0) {
                float avgY = yBottom + (average / maxValue) * chartHeight;
                cb.setColorStroke(new Color(0xD3, 0x2F, 0x2F));
                cb.setLineWidth(1.5f);
                cb.setLineDash(6f, 4f, 0f);
                cb.moveTo(chartLeft, avgY);
                cb.lineTo(chartRight, avgY);
                cb.stroke();
                cb.setLineDash(0);

                cb.beginText();
                cb.setFontAndSize(bf, 8);
                cb.setColorFill(new Color(0xD3, 0x2F, 0x2F));
                cb.showTextAligned(Element.ALIGN_RIGHT, String.format("Avg: %.1f", average), chartRight, avgY + 3, 0);
                cb.endText();
            }

            // X-axis labels
            int labelStep = Math.max(1, count / 10);
            for (int i = 0; i < count; i += labelStep) {
                float centerX = chartLeft + barSpacing * i + barSpacing / 2;
                if (i < labels.size()) {
                    cb.beginText();
                    cb.setFontAndSize(bf, 7);
                    cb.setColorFill(Color.DARK_GRAY);
                    cb.showTextAligned(Element.ALIGN_CENTER, labels.get(i), centerX, yBottom - 10, 0);
                    cb.endText();
                }
            }
        } catch (IOException e) {
            throw new DocumentException(e);
        }
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(new Color(0x38, 0x8E, 0x3C));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_BODY_FONT));
        cell.setPadding(4);
        table.addCell(cell);
    }
}
