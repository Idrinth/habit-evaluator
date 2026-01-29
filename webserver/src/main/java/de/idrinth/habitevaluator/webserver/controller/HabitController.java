package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitRepository habitRepository;
    private final HabitEvaluatorService evaluatorService;

    public HabitController(HabitRepository habitRepository, HabitEvaluatorService evaluatorService) {
        this.habitRepository = habitRepository;
        this.evaluatorService = evaluatorService;
    }

    @GetMapping
    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(@PathVariable String id) {
        return habitRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Habit createHabit(@RequestBody Habit habit) {
        return habitRepository.save(habit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable String id, @RequestBody Habit habit) {
        if (!habitRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        habit.setId(id);
        return ResponseEntity.ok(habitRepository.save(habit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable String id) {
        if (!habitRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        habitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/entries")
    public ResponseEntity<HabitEntry> addEntry(@PathVariable String id, @RequestBody HabitEntry entry) {
        return habitRepository.findById(id)
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
            @RequestParam(required = false) LocalDate end) {

        return habitRepository.findById(id)
                .map(habit -> {
                    LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
                    LocalDate endDate = end != null ? end : LocalDate.now();
                    Evaluation evaluation = evaluatorService.evaluate(habit, startDate, endDate);
                    return ResponseEntity.ok(evaluation);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
