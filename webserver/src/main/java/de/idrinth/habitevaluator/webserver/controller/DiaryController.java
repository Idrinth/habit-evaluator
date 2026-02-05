package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
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
    private final UserRepository userRepository;
    private final DiaryService diaryService;

    public DiaryController(DiaryEntryRepository diaryEntryRepository,
                           UserRepository userRepository,
                           DiaryService diaryService) {
        this.diaryEntryRepository = diaryEntryRepository;
        this.userRepository = userRepository;
        this.diaryService = diaryService;
    }

    @GetMapping
    public ResponseEntity<List<DiaryEntry>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
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
        entry.setUser(userOpt.get());
        return ResponseEntity.ok(diaryEntryRepository.save(entry));
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
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(diaryEntryRepository.findDistinctDescriptionsByUserId(userId));
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
        stats.put("monthlyTrend", diaryService.getMonthlyTrend(entries));
        return ResponseEntity.ok(stats);
    }
}
