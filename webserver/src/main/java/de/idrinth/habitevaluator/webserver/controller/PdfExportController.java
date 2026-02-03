package de.idrinth.habitevaluator.webserver.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import de.idrinth.habitevaluator.shared.model.SleepStats;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/export")
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

    private final HabitRepository habitRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final SleepEntryRepository sleepEntryRepository;
    private final HabitScoringService scoringService;
    private final DiaryService diaryService;
    private final SleepEvaluationService sleepEvaluationService;

    public PdfExportController(HabitRepository habitRepository,
                               DiaryEntryRepository diaryEntryRepository,
                               SleepEntryRepository sleepEntryRepository,
                               HabitScoringService scoringService,
                               DiaryService diaryService,
                               SleepEvaluationService sleepEvaluationService) {
        this.habitRepository = habitRepository;
        this.diaryEntryRepository = diaryEntryRepository;
        this.sleepEntryRepository = sleepEntryRepository;
        this.scoringService = scoringService;
        this.diaryService = diaryService;
        this.sleepEvaluationService = sleepEvaluationService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "true") boolean habits,
            @RequestParam(defaultValue = "true") boolean sleep,
            @RequestParam(defaultValue = "true") boolean diary,
            HttpSession session) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (!habits && !sleep && !diary) {
            return ResponseEntity.badRequest().build();
        }

        LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
        LocalDate fromDate = from != null ? LocalDate.parse(from) : toDate.minusDays(29);

        try {
            byte[] pdf = generatePdf(userId, fromDate, toDate, habits, sleep, diary);
            String fileName = "habit_report_" + fromDate.format(DISPLAY_FORMAT) + "_to_" + toDate.format(DISPLAY_FORMAT) + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generatePdf(String userId, LocalDate fromDate, LocalDate toDate,
                                boolean includeHabits, boolean includeSleep, boolean includeDiary) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Title
        Paragraph title = new Paragraph("Habit Evaluator Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_LEFT);
        document.add(title);

        // Date range
        String dateRange = fromDate.format(DISPLAY_FORMAT) + " \u2014 " + toDate.format(DISPLAY_FORMAT);
        Paragraph subtitle = new Paragraph(dateRange, SUBTITLE_FONT);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        if (includeHabits) {
            addHabitSection(document, writer, userId, fromDate, toDate);
        }

        if (includeSleep) {
            addSleepSection(document, writer, userId, fromDate, toDate);
        }

        if (includeDiary) {
            addDiarySection(document, writer, userId, fromDate, toDate);
        }

        document.close();
        return baos.toByteArray();
    }

    private void addHabitSection(Document document, PdfWriter writer, String userId,
                                  LocalDate fromDate, LocalDate toDate) throws DocumentException, IOException {
        List<Habit> habitList = habitRepository.findByUserId(userId);

        Paragraph header = new Paragraph("Habits", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(10);
        document.add(header);

        if (habitList.isEmpty()) {
            document.add(new Paragraph("No habit data available.", BODY_FONT));
            return;
        }

        // Build daily data
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

        // Draw chart
        drawBarChart(writer, document, labels, values, avgPoints, new Color(0x38, 0x8E, 0x3C), "Habit Points");

        // Summary stats
        Paragraph stats = new Paragraph(
                String.format("Total: %d pts  |  Average: %.1f pts/day", (int) totalPoints, avgPoints),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        // Per-habit breakdown table
        Paragraph breakdownTitle = new Paragraph("Per-Habit Breakdown", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
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
                                  LocalDate fromDate, LocalDate toDate) throws DocumentException, IOException {
        List<SleepEntry> allEntries = sleepEntryRepository.findByUserId(userId);

        Paragraph header = new Paragraph("Sleep", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(10);
        document.add(header);

        // Build daily data
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

        // Draw chart
        drawBarChart(writer, document, labels, durations, avgDuration, new Color(0x4C, 0xAF, 0x50), "Sleep Duration (hours)");

        // Summary
        Paragraph stats = new Paragraph(
                String.format("Avg: %.1f h  |  Min: %.1f h  |  Max: %.1f h", avgDuration, minDuration, maxDuration),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        // Sleep entries list
        List<SleepEntry> rangeEntries = new ArrayList<>();
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(fromDate) && !entry.getDate().isAfter(toDate)) {
                rangeEntries.add(entry);
            }
        }

        if (!rangeEntries.isEmpty()) {
            Paragraph entriesTitle = new Paragraph("Sleep Entries", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
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
                                  LocalDate fromDate, LocalDate toDate) throws DocumentException, IOException {
        List<DiaryEntry> allEntries = diaryEntryRepository.findByUserId(userId);

        Paragraph header = new Paragraph("Diary", SECTION_FONT);
        header.setSpacingBefore(15);
        header.setSpacingAfter(10);
        document.add(header);

        // Build daily data
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

        // Draw chart
        drawBarChart(writer, document, labels, diaryPoints, avgPoints, new Color(0x66, 0xBB, 0x6A), "Diary Points");

        // Summary
        Paragraph stats = new Paragraph(
                String.format("Total: %d pts  |  Average: %.1f pts/day", (int) totalPoints, avgPoints),
                BODY_FONT);
        stats.setSpacingAfter(10);
        document.add(stats);

        // Diary entries list
        List<DiaryEntry> rangeEntries = diaryService.getEntriesInRange(allEntries, fromDate, toDate);

        if (!rangeEntries.isEmpty()) {
            Paragraph entriesTitle = new Paragraph("Diary Entries", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
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

    private void drawBarChart(PdfWriter writer, Document document, List<String> labels,
                               List<Float> values, float average, Color barColor, String chartTitle) throws DocumentException, IOException {
        float chartWidth = PageSize.A4.getWidth() - 80;
        float chartHeight = 160f;
        float totalHeight = chartHeight + 40;

        // Add spacing paragraph to reserve space
        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(totalHeight);
        document.add(spacer);

        PdfContentByte cb = writer.getDirectContent();
        float pageHeight = document.getPageSize().getHeight();
        float xStart = document.leftMargin();
        float yBottom = pageHeight - document.topMargin() - writer.getVerticalPosition(false) + totalHeight - chartHeight - 15;
        float yTop = yBottom + chartHeight;

        // Chart title
        cb.beginText();
        cb.setFontAndSize(com.lowagie.text.pdf.BaseFont.createFont(
                com.lowagie.text.pdf.BaseFont.HELVETICA_BOLD,
                com.lowagie.text.pdf.BaseFont.CP1252,
                com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED), 11);
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
            cb.setFontAndSize(com.lowagie.text.pdf.BaseFont.createFont(
                    com.lowagie.text.pdf.BaseFont.HELVETICA,
                    com.lowagie.text.pdf.BaseFont.CP1252,
                    com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED), 7);
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
            float top = yBottom + barHeight;

            cb.setColorFill(barColor);
            cb.rectangle(left, yBottom, right - left, top - yBottom);
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
            cb.setFontAndSize(com.lowagie.text.pdf.BaseFont.createFont(
                    com.lowagie.text.pdf.BaseFont.HELVETICA,
                    com.lowagie.text.pdf.BaseFont.CP1252,
                    com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED), 8);
            cb.setColorFill(new Color(0xD3, 0x2F, 0x2F));
            cb.showTextAligned(Element.ALIGN_RIGHT, String.format("Avg: %.1f", average), chartRight, avgY + 3, 0);
            cb.endText();
        }

        // X-axis labels
        int labelStep = Math.max(1, count / 10);
        for (int i = 0; i < count; i += labelStep) {
            float centerX = chartLeft + barSpacing * i + barSpacing / 2;
            if (i < labels.size()) {
                cb.saveState();
                cb.beginText();
                cb.setFontAndSize(com.lowagie.text.pdf.BaseFont.createFont(
                        com.lowagie.text.pdf.BaseFont.HELVETICA,
                        com.lowagie.text.pdf.BaseFont.CP1252,
                        com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED), 7);
                cb.setColorFill(Color.DARK_GRAY);
                cb.showTextAligned(Element.ALIGN_CENTER, labels.get(i), centerX, yBottom - 10, 0);
                cb.endText();
                cb.restoreState();
            }
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
