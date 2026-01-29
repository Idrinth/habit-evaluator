package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final Map<String, Habit> habitStore = new ConcurrentHashMap<>();
    private final HabitEvaluatorService evaluatorService = new HabitEvaluatorService();

    @GetMapping
    public List<Habit> getAllHabits() {
        return new ArrayList<>(habitStore.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(@PathVariable String id) {
        Habit habit = habitStore.get(id);
        if (habit == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(habit);
    }

    @PostMapping
    public Habit createHabit(@RequestBody Habit habit) {
        habitStore.put(habit.getId(), habit);
        return habit;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable String id, @RequestBody Habit habit) {
        if (!habitStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        habit.setId(id);
        habitStore.put(id, habit);
        return ResponseEntity.ok(habit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable String id) {
        if (!habitStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        habitStore.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/entries")
    public ResponseEntity<HabitEntry> addEntry(@PathVariable String id, @RequestBody HabitEntry entry) {
        Habit habit = habitStore.get(id);
        if (habit == null) {
            return ResponseEntity.notFound().build();
        }
        entry.setHabitId(id);
        habit.addEntry(entry);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/{id}/evaluate")
    public ResponseEntity<Evaluation> evaluateHabit(
            @PathVariable String id,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {

        Habit habit = habitStore.get(id);
        if (habit == null) {
            return ResponseEntity.notFound().build();
        }

        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }

        Evaluation evaluation = evaluatorService.evaluate(habit, start, end);
        return ResponseEntity.ok(evaluation);
    }
}
