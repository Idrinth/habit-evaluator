package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SleepStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sleep-entries")
public class SleepEntryController {

    private final SleepEntryRepository sleepEntryRepository;
    private final UserRepository userRepository;
    private final SleepEvaluationService sleepEvaluationService;

    public SleepEntryController(SleepEntryRepository sleepEntryRepository,
                                UserRepository userRepository,
                                SleepEvaluationService sleepEvaluationService) {
        this.sleepEntryRepository = sleepEntryRepository;
        this.userRepository = userRepository;
        this.sleepEvaluationService = sleepEvaluationService;
    }

    @GetMapping
    public ResponseEntity<List<SleepEntry>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(sleepEntryRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody SleepEntry entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        entry.setUser(userOpt.get());

        List<SleepEntry> existing = sleepEntryRepository.findByUserId(userId);
        if (sleepEvaluationService.hasOverlap(existing, entry.getDate(), entry.getFromTime(), entry.getUntilTime())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Sleep entry overlaps with an existing entry");
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok(sleepEntryRepository.save(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<SleepEntry> entryOpt = sleepEntryRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SleepEntry entry = entryOpt.get();
        if (entry.getUser() == null || !userId.equals(entry.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        sleepEntryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, SleepStats>> getStats(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<SleepEntry> entries = sleepEntryRepository.findByUserId(userId);
        Map<String, SleepStats> stats = new HashMap<>();
        stats.put("weekly", sleepEvaluationService.getCurrentWeekStats(entries));
        stats.put("monthly", sleepEvaluationService.getCurrentMonthStats(entries));
        return ResponseEntity.ok(stats);
    }
}
