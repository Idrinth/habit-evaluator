package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.GratitudeService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/gratitude")
public class GratitudeController {

    private static final String GRATITUDE_STATS = "gratitudeStats";

    private final GratitudeEntryRepository gratitudeEntryRepository;
    private final UserRepository userRepository;
    private final GratitudeService gratitudeService;
    private final StatsCacheService statsCacheService;

    public GratitudeController(GratitudeEntryRepository gratitudeEntryRepository,
                               UserRepository userRepository,
                               GratitudeService gratitudeService,
                               StatsCacheService statsCacheService) {
        this.gratitudeEntryRepository = gratitudeEntryRepository;
        this.userRepository = userRepository;
        this.gratitudeService = gratitudeService;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping
    public ResponseEntity<List<GratitudeEntry>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(gratitudeEntryRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<GratitudeEntry> createEntry(@RequestBody GratitudeEntry entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        entry.setId(UUID.randomUUID().toString());
        entry.setUser(user);

        GratitudeEntry saved = gratitudeEntryRepository.save(entry);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<GratitudeEntry> entryOpt = gratitudeEntryRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GratitudeEntry existing = entryOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        gratitudeEntryRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> cached = statsCacheService.getCachedMap(userId, GRATITUDE_STATS);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        List<GratitudeEntry> entries = gratitudeEntryRepository.findByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayCount", gratitudeService.getDayCount(entries, LocalDate.now()));
        stats.put("weekCount", gratitudeService.getCurrentWeekCount(entries));
        stats.put("monthCount", gratitudeService.getCurrentMonthCount(entries));
        stats.put("dailyAverage", gratitudeService.getDailyAverageForMonth(entries));
        stats.put("currentStreak", gratitudeService.getCurrentStreak(entries));
        statsCacheService.putMap(userId, GRATITUDE_STATS, stats);
        return ResponseEntity.ok(stats);
    }
}
