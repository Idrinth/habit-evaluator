package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private static final int DAYS = 30;
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    private final HabitRepository habitRepository;
    private final SleepEntryRepository sleepEntryRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final HabitCategoryRepository habitCategoryRepository;
    private final HabitScoringService scoringService;
    private final DiaryService diaryService;
    private final EventCorrelationService correlationService;

    public StatsController(HabitRepository habitRepository,
                           SleepEntryRepository sleepEntryRepository,
                           DiaryEntryRepository diaryEntryRepository,
                           HabitCategoryRepository habitCategoryRepository,
                           HabitScoringService scoringService,
                           DiaryService diaryService,
                           EventCorrelationService correlationService) {
        this.habitRepository = habitRepository;
        this.sleepEntryRepository = sleepEntryRepository;
        this.diaryEntryRepository = diaryEntryRepository;
        this.habitCategoryRepository = habitCategoryRepository;
        this.scoringService = scoringService;
        this.diaryService = diaryService;
        this.correlationService = correlationService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS - 1);

        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("habitPoints", calculateHabitPoints(userId, startDate, today));
        result.put("diaryPoints", calculateDiaryPoints(userId, startDate, today));
        result.put("sleepDuration", calculateSleepDuration(userId, startDate, today));
        result.put("sleepEntries", calculateSleepEntries(userId, startDate, today));

        return ResponseEntity.ok(result);
    }

    private List<Integer> calculateHabitPoints(String userId, LocalDate startDate, LocalDate today) {
        List<Habit> habits = habitRepository.findByUserId(userId);
        List<Integer> points = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            int dayTotal = 0;
            for (Habit habit : habits) {
                dayTotal += scoringService.calculateHabitScore(habit, d, d).getScore();
            }
            points.add(dayTotal);
        }
        return points;
    }

    private List<Integer> calculateDiaryPoints(String userId, LocalDate startDate, LocalDate today) {
        List<DiaryEntry> allEntries = diaryEntryRepository.findByUserId(userId);
        List<Integer> points = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            points.add(diaryService.getDayPoints(allEntries, d));
        }
        return points;
    }

    private List<Double> calculateSleepDuration(String userId, LocalDate startDate, LocalDate today) {
        List<SleepEntry> allEntries = sleepEntryRepository.findByUserId(userId);
        Map<LocalDate, Double> durationByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            durationByDate.put(d, 0.0);
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                durationByDate.merge(entry.getDate(), entry.getHours(), Double::sum);
            }
        }
        return new ArrayList<>(durationByDate.values());
    }

    private List<Integer> calculateSleepEntries(String userId, LocalDate startDate, LocalDate today) {
        List<SleepEntry> allEntries = sleepEntryRepository.findByUserId(userId);
        Map<LocalDate, Integer> countByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            countByDate.put(d, 0);
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                countByDate.merge(entry.getDate(), 1, Integer::sum);
            }
        }
        return new ArrayList<>(countByDate.values());
    }

    @GetMapping("/daily-timeline")
    public ResponseEntity<Map<String, Object>> getDailyTimeline(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS - 1);

        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }

        List<Habit> habits = habitRepository.findByUserId(userId);
        Map<String, String> categoryColors = new LinkedHashMap<>();
        for (HabitCategory cat : habitCategoryRepository.findByUserId(userId)) {
            if (cat.getColor() != null) {
                categoryColors.put(cat.getId(), cat.getColor());
            }
        }

        String[] defaultColors = {
            "#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0",
            "#00BCD4", "#FF5722", "#795548", "#607D8B", "#8BC34A"
        };

        List<Map<String, Object>> habitTimelines = new ArrayList<>();
        int colorIndex = 0;
        for (Habit habit : habits) {
            List<Map<String, Object>> entries = new ArrayList<>();
            for (HabitEntry entry : habit.getEntries()) {
                LocalDate entryDate = entry.getCompletedAt().toLocalDate();
                if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                    int dayIndex = (int) ChronoUnit.DAYS.between(startDate, entryDate);
                    double hour = entry.getCompletedAt().getHour()
                            + entry.getCompletedAt().getMinute() / 60.0;
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("dayIndex", dayIndex);
                    point.put("hour", hour);
                    entries.add(point);
                }
            }
            if (!entries.isEmpty()) {
                String color = categoryColors.getOrDefault(
                        habit.getCategoryId(),
                        defaultColors[colorIndex % defaultColors.length]
                );
                Map<String, Object> timeline = new LinkedHashMap<>();
                timeline.put("habitId", habit.getId());
                timeline.put("habitName", habit.getName());
                timeline.put("color", color);
                timeline.put("entries", entries);
                habitTimelines.add(timeline);
                colorIndex++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("habits", habitTimelines);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/correlations")
    public ResponseEntity<List<Map<String, Object>>> getCorrelations(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Habit> habits = habitRepository.findByUserId(userId);
        List<DiaryEntry> diaryEntries = diaryEntryRepository.findByUserId(userId);
        List<SleepEntry> sleepEntries = sleepEntryRepository.findByUserId(userId);

        List<EventCorrelation> correlations = correlationService.calculateCorrelations(
                habits, diaryEntries, sleepEntries);

        List<Map<String, Object>> result = new ArrayList<>();
        for (EventCorrelation corr : correlations) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("eventA", corr.getEventA());
            entry.put("eventB", corr.getEventB());
            entry.put("correlation", Math.round(corr.getCorrelation() * 1000.0) / 1000.0);
            entry.put("sharedDays", corr.getSharedDays());
            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }
}
