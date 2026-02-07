package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.model.SportLogStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.SportLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sport-logs")
public class SportLogController {

    private final SportLogRepository sportLogRepository;
    private final UserRepository userRepository;
    private final SportLogService sportLogService;

    public SportLogController(SportLogRepository sportLogRepository,
                              UserRepository userRepository,
                              SportLogService sportLogService) {
        this.sportLogRepository = sportLogRepository;
        this.userRepository = userRepository;
        this.sportLogService = sportLogService;
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

        return ResponseEntity.ok(sportLogRepository.save(entry));
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
        return ResponseEntity.noContent().build();
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
