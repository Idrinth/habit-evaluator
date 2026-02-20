package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryEntryRepository diaryEntryRepository;
    private final DiaryReferenceRepository diaryReferenceRepository;
    private final UserRepository userRepository;
    private final DiaryService diaryService;
    private final StatsCacheService statsCacheService;

    public DiaryController(DiaryEntryRepository diaryEntryRepository,
                           DiaryReferenceRepository diaryReferenceRepository,
                           UserRepository userRepository,
                           DiaryService diaryService,
                           StatsCacheService statsCacheService) {
        this.diaryEntryRepository = diaryEntryRepository;
        this.diaryReferenceRepository = diaryReferenceRepository;
        this.userRepository = userRepository;
        this.diaryService = diaryService;
        this.statsCacheService = statsCacheService;
    }

    /**
     * Migrates entries with legacy descriptions to use diary references.
     * This is called on first read to ensure old data is converted.
     */
    private void migrateEntriesIfNeeded(String userId) {
        List<DiaryEntry> entriesNeedingMigration = diaryEntryRepository.findEntriesNeedingMigration(userId);
        if (entriesNeedingMigration.isEmpty()) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        for (DiaryEntry entry : entriesNeedingMigration) {
            String description = entry.getLegacyDescription();
            if (description != null && !description.isEmpty()) {
                DiaryReference reference = diaryReferenceRepository.findOrCreate(
                        userId, description, () -> user);
                entry.setDiaryReference(reference);
                entry.setLegacyDescription(null);
                diaryEntryRepository.save(entry);
            }
        }
    }

    @GetMapping
    public ResponseEntity<List<DiaryEntry>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        migrateEntriesIfNeeded(userId);
        return ResponseEntity.ok(diaryEntryRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<DiaryEntry> createEntry(@RequestBody DiaryEntry entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        entry.setUser(user);

        // Convert description to reference for new entries
        String description = entry.getDescription();
        if (description != null && !description.isEmpty() && entry.getDiaryReference() == null) {
            DiaryReference reference = diaryReferenceRepository.findOrCreate(userId, description, () -> user);
            entry.setDiaryReference(reference);
            entry.setLegacyDescription(null);
        }

        DiaryEntry saved = diaryEntryRepository.save(entry);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<DiaryEntry> entryOpt = diaryEntryRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DiaryEntry existing = entryOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        diaryEntryRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        // Migrate first to ensure suggestions come from references
        migrateEntriesIfNeeded(userId);
        return ResponseEntity.ok(diaryReferenceRepository.findDistinctDescriptionsByUserId(userId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<DiaryEntry> entries = diaryEntryRepository.findByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayPoints", diaryService.getDayPoints(entries, LocalDate.now()));
        stats.put("weekPoints", diaryService.getCurrentWeekPoints(entries));
        stats.put("monthPoints", diaryService.getCurrentMonthPoints(entries));
        stats.put("weeklyAverage", diaryService.getWeeklyAverageForMonth(entries));
        stats.put("dailyAverage", diaryService.getDailyAverageForMonth(entries));
        stats.put("monthlyTrend", diaryService.getMonthlyTrend(entries));
        return ResponseEntity.ok(stats);
    }
}
