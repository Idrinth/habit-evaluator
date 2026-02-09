package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.MagicLink;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.MagicLinkRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class MagicLinkController {

    private final MagicLinkRepository magicLinkRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public MagicLinkController(MagicLinkRepository magicLinkRepository,
                               HabitRepository habitRepository,
                               UserRepository userRepository) {
        this.magicLinkRepository = magicLinkRepository;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new magic link for sharing data.
     * Requires authentication.
     */
    @PostMapping("/api/magic-links")
    public ResponseEntity<MagicLink> createMagicLink(@RequestBody MagicLinkRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        MagicLink magicLink = new MagicLink();
        magicLink.setUser(userOpt.get());
        magicLink.setCategoryId(request.categoryId);
        magicLink.setFilterStart(request.filterStart);
        magicLink.setFilterEnd(request.filterEnd);
        magicLink.setExpiresAt(request.expiresAt);

        return ResponseEntity.ok(magicLinkRepository.save(magicLink));
    }

    /**
     * List all magic links for the authenticated user.
     */
    @GetMapping("/api/magic-links")
    public ResponseEntity<List<MagicLink>> listMagicLinks(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(magicLinkRepository.findByUserId(userId));
    }

    /**
     * Delete a magic link owned by the authenticated user.
     */
    @DeleteMapping("/api/magic-links/{id}")
    public ResponseEntity<Void> deleteMagicLink(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<MagicLink> linkOpt = magicLinkRepository.findById(id);
        if (linkOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MagicLink link = linkOpt.get();
        if (link.getUser() == null || !userId.equals(link.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        magicLinkRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Public endpoint: access shared habit data via magic link token.
     * Returns read-only habit data filtered by the magic link's time period and category.
     */
    @GetMapping("/api/shared/{token}")
    public ResponseEntity<Map<String, Object>> getSharedData(@PathVariable String token) {
        Optional<MagicLink> linkOpt = magicLinkRepository.findByToken(token);
        if (linkOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MagicLink link = linkOpt.get();
        if (link.isExpired()) {
            return ResponseEntity.status(410).build();
        }
        if (link.getUser() == null) {
            return ResponseEntity.notFound().build();
        }

        String userId = link.getUser().getId();
        List<Habit> habits = habitRepository.findByUserId(userId);

        // Filter by category if specified
        if (link.getCategoryId() != null) {
            habits = habits.stream()
                    .filter(h -> link.getCategoryId().equals(h.getCategoryId()))
                    .collect(Collectors.toList());
        }

        // Filter entries by time period
        LocalDate filterStart = link.getFilterStart();
        LocalDate filterEnd = link.getFilterEnd();

        List<Map<String, Object>> habitData = new ArrayList<>();
        for (Habit habit : habits) {
            Map<String, Object> habitMap = new HashMap<>();
            habitMap.put("id", habit.getId());
            habitMap.put("name", habit.getName());
            habitMap.put("description", habit.getDescription());
            habitMap.put("categoryId", habit.getCategoryId());
            habitMap.put("frequencyType", habit.getFrequencyType());
            habitMap.put("targetFrequency", habit.getTargetFrequency());
            habitMap.put("createdAt", habit.getCreatedAt());

            List<HabitEntry> entries = habit.getEntries();
            if (filterStart != null || filterEnd != null) {
                entries = entries.stream()
                        .filter(e -> {
                            LocalDate entryDate = e.getCompletedAt().toLocalDate();
                            if (filterStart != null && entryDate.isBefore(filterStart)) {
                                return false;
                            }
                            if (filterEnd != null && entryDate.isAfter(filterEnd)) {
                                return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toList());
            }

            List<Map<String, Object>> entryData = entries.stream()
                    .map(e -> {
                        Map<String, Object> entryMap = new HashMap<>();
                        entryMap.put("id", e.getId());
                        entryMap.put("completedAt", e.getCompletedAt());
                        entryMap.put("notes", e.getNotes());
                        entryMap.put("value", e.getValue());
                        return entryMap;
                    })
                    .collect(Collectors.toList());

            habitMap.put("entries", entryData);
            habitData.add(habitMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("filterStart", link.getFilterStart());
        response.put("filterEnd", link.getFilterEnd());
        response.put("categoryId", link.getCategoryId());
        response.put("habits", habitData);
        return ResponseEntity.ok(response);
    }

    /**
     * Request body for creating a magic link.
     */
    public static class MagicLinkRequest {
        public String categoryId;
        public LocalDate filterStart;
        public LocalDate filterEnd;
        public java.time.LocalDateTime expiresAt;
    }
}
