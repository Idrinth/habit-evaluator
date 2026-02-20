package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.SportLogStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.SportLogService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/sport-logs")
public class SportLogController {

    private final SportLogRepository sportLogRepository;
    private final UserRepository userRepository;
    private final SportLogService sportLogService;
    private final StatsCacheService statsCacheService;

    public SportLogController(SportLogRepository sportLogRepository,
                              UserRepository userRepository,
                              SportLogService sportLogService,
                              StatsCacheService statsCacheService) {
        this.sportLogRepository = sportLogRepository;
        this.userRepository = userRepository;
        this.sportLogService = sportLogService;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, List<String>>> getSuggestions(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, List<String>> suggestions = new HashMap<>();
        suggestions.put("names", sportLogRepository.findDistinctNamesByUserId(userId));
        suggestions.put("units", sportLogRepository.findDistinctMeasurementUnitsByUserId(userId));
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping
    public ResponseEntity<List<SportLog>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(sportLogRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody SportLog entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        entry.setUser(userOpt.get());

        if (entry.getName() == null || entry.getName().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Sport activity name is required");
            return ResponseEntity.badRequest().body(error);
        }
        if (entry.getMeasurementUnit() == null || entry.getMeasurementUnit().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Measurement unit is required");
            return ResponseEntity.badRequest().body(error);
        }

        SportLog saved = sportLogRepository.save(entry);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<SportLog> entryOpt = sportLogRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SportLog entry = entryOpt.get();
        if (entry.getUser() == null || !userId.equals(entry.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        sportLogRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getGraph(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<SportLog> entries = sportLogRepository.findByUserId(userId);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(labelFormat));
        }

        // Group entries by activity name within the date range
        Map<String, List<SportLog>> entriesByActivity = new LinkedHashMap<>();
        for (SportLog entry : entries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                entriesByActivity.computeIfAbsent(entry.getName(), k -> new ArrayList<>()).add(entry);
            }
        }

        String[] defaultColors = {
            "#FF9800", "#4CAF50", "#2196F3", "#E91E63", "#9C27B0",
            "#00BCD4", "#FF5722", "#795548", "#607D8B", "#8BC34A"
        };

        List<Map<String, Object>> activities = new ArrayList<>();
        int colorIndex = 0;
        for (Map.Entry<String, List<SportLog>> activityEntry : entriesByActivity.entrySet()) {
            String activityName = activityEntry.getKey();
            List<SportLog> activityLogs = activityEntry.getValue();

            // Determine the measurement unit from the first entry
            String unit = activityLogs.get(0).getMeasurementUnit();

            // Build daily maps
            Map<LocalDate, Double> durationByDate = new TreeMap<>();
            Map<LocalDate, Double> measurementByDate = new TreeMap<>();
            Map<LocalDate, Integer> countByDate = new TreeMap<>();
            for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
                durationByDate.put(d, 0.0);
                measurementByDate.put(d, 0.0);
                countByDate.put(d, 0);
            }

            for (SportLog log : activityLogs) {
                durationByDate.merge(log.getDate(), log.getDurationHours(), Double::sum);
                measurementByDate.merge(log.getDate(), log.getMeasurement(), Double::sum);
                countByDate.merge(log.getDate(), 1, Integer::sum);
            }

            Map<String, Object> activity = new LinkedHashMap<>();
            activity.put("name", activityName);
            activity.put("unit", unit);
            activity.put("color", defaultColors[colorIndex % defaultColors.length]);
            activity.put("dailyDuration", new ArrayList<>(durationByDate.values()));
            activity.put("dailyMeasurement", new ArrayList<>(measurementByDate.values()));
            activity.put("dailyEntries", new ArrayList<>(countByDate.values()));
            activities.add(activity);
            colorIndex++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("activities", activities);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, SportLogStats>> getStats(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<SportLog> entries = sportLogRepository.findByUserId(userId);
        Map<String, SportLogStats> stats = new HashMap<>();
        stats.put("weekly", sportLogService.getCurrentWeekStats(entries));
        stats.put("monthly", sportLogService.getCurrentMonthStats(entries));
        return ResponseEntity.ok(stats);
    }
}
