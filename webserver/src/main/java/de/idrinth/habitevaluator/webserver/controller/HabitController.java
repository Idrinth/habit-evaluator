package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.PredictedWeeklyScore;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final HabitEvaluatorService evaluatorService;
    private final HabitScoringService scoringService;

    public HabitController(HabitRepository habitRepository, UserRepository userRepository,
                           HabitEvaluatorService evaluatorService, HabitScoringService scoringService) {
        this.habitRepository = habitRepository;
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
        habitRepository.deleteById(id);
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
                    habit.addEntry(entry);
                    habitRepository.save(habit);
                    return ResponseEntity.ok(entry);
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
}
