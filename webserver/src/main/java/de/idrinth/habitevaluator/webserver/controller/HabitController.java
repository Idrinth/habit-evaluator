package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.PredictedWeeklyScore;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitRepository habitRepository;
    private final HabitCategoryRepository habitCategoryRepository;
    private final UserRepository userRepository;
    private final HabitEvaluatorService evaluatorService;
    private final HabitScoringService scoringService;

    public HabitController(HabitRepository habitRepository, HabitCategoryRepository habitCategoryRepository,
                           UserRepository userRepository,
                           HabitEvaluatorService evaluatorService, HabitScoringService scoringService) {
        this.habitRepository = habitRepository;
        this.habitCategoryRepository = habitCategoryRepository;
        this.userRepository = userRepository;
        this.evaluatorService = evaluatorService;
        this.scoringService = scoringService;
    }

    @GetMapping
    public ResponseEntity<List<Habit>> getAllHabits(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(habitRepository.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return habitRepository.findById(id)
                .filter(habit -> userId.equals(habit.getUser() != null ? habit.getUser().getId() : null))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(@RequestBody Habit habit, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        habit.setUser(userOpt.get());
        return ResponseEntity.ok(habitRepository.save(habit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable String id, @RequestBody Habit habit, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<Habit> existingOpt = habitRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Habit existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        habit.setId(id);
        habit.setUser(existing.getUser());
        return ResponseEntity.ok(habitRepository.save(habit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<Habit> existingOpt = habitRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Habit existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        String categoryId = existing.getCategoryId();
        habitRepository.deleteById(id);
        if (categoryId != null && !categoryId.isEmpty()) {
            boolean categoryStillUsed = habitRepository.findByUserId(userId).stream()
                    .anyMatch(h -> categoryId.equals(h.getCategoryId()));
            if (!categoryStillUsed) {
                habitCategoryRepository.deleteById(categoryId);
            }
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/entries")
    public ResponseEntity<HabitEntry> addEntry(@PathVariable String id, @RequestBody HabitEntry entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return habitRepository.findById(id)
                .filter(habit -> userId.equals(habit.getUser() != null ? habit.getUser().getId() : null))
                .map(habit -> {
                    if (habit.hasReachedDailyLimit(LocalDate.now())) {
                        return ResponseEntity.badRequest().<HabitEntry>body(null);
                    }
                    habit.addEntry(entry);
                    habitRepository.save(habit);
                    return ResponseEntity.ok(entry);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/entries/last")
    public ResponseEntity<Void> removeLastEntry(@PathVariable String id,
                                                 @RequestParam LocalDate date,
                                                 HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return habitRepository.findById(id)
                .filter(habit -> userId.equals(habit.getUser() != null ? habit.getUser().getId() : null))
                .map(habit -> {
                    if (habit.removeLastEntryForDate(date)) {
                        habitRepository.save(habit);
                        return ResponseEntity.noContent().<Void>build();
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/evaluate")
    public ResponseEntity<Evaluation> evaluateHabit(
            @PathVariable String id,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            HttpSession session) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return habitRepository.findById(id)
                .filter(habit -> userId.equals(habit.getUser() != null ? habit.getUser().getId() : null))
                .map(habit -> {
                    LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
                    LocalDate endDate = end != null ? end : LocalDate.now();
                    Evaluation evaluation = evaluatorService.evaluate(habit, startDate, endDate);
                    return ResponseEntity.ok(evaluation);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/predict")
    public ResponseEntity<PredictedWeeklyScore> predictWeeklyScore(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<Habit> habits = habitRepository.findByUserId(userId);
        PredictedWeeklyScore prediction = scoringService.predictCurrentWeekScore(habits);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/{id}/point-development")
    public ResponseEntity<Map<String, Object>> getPointDevelopment(
            @PathVariable String id,
            @RequestParam(defaultValue = "week") String period,
            HttpSession session) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return habitRepository.findById(id)
                .filter(habit -> userId.equals(habit.getUser() != null ? habit.getUser().getId() : null))
                .map(habit -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start;
                    LocalDate end;
                    if ("month".equals(period)) {
                        start = today.with(TemporalAdjusters.firstDayOfMonth());
                        end = today.with(TemporalAdjusters.lastDayOfMonth());
                    } else {
                        start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                        end = start.plusDays(6);
                    }

                    List<Integer> dailyPoints = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    DateTimeFormatter dayFormat = "month".equals(period)
                            ? DateTimeFormatter.ofPattern("d")
                            : DateTimeFormatter.ofPattern("EEE");

                    int daysInPeriod = (int) (start.until(end, java.time.temporal.ChronoUnit.DAYS)) + 1;
                    int totalPoints = 0;

                    for (int i = 0; i < daysInPeriod; i++) {
                        LocalDate date = start.plusDays(i);
                        int dayScore = scoringService.calculateHabitScore(habit, date, date).getScore();
                        dailyPoints.add(dayScore);
                        totalPoints += dayScore;

                        if ("month".equals(period)) {
                            if (i == 0 || i == daysInPeriod - 1 || (i + 1) % 5 == 0) {
                                labels.add(date.format(dayFormat));
                            } else {
                                labels.add("");
                            }
                        } else {
                            labels.add(date.format(dayFormat));
                        }
                    }

                    double average = totalPoints / (double) daysInPeriod;

                    List<Integer> runningAverages = new ArrayList<>();
                    int runningTotal = 0;
                    for (int i = 0; i < dailyPoints.size(); i++) {
                        runningTotal += dailyPoints.get(i);
                        runningAverages.add(Math.round((float) runningTotal / (i + 1)));
                    }

                    List<Integer> cumulativeTotals = new ArrayList<>();
                    int cumulative = 0;
                    for (int points : dailyPoints) {
                        cumulative += points;
                        cumulativeTotals.add(cumulative);
                    }

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("dailyPoints", dailyPoints);
                    result.put("labels", labels);
                    result.put("runningAverages", runningAverages);
                    result.put("cumulativeTotals", cumulativeTotals);
                    result.put("totalPoints", totalPoints);
                    result.put("average", Math.round(average * 10.0) / 10.0);

                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
