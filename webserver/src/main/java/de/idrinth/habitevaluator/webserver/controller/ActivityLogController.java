package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.model.PersonTag;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import de.idrinth.habitevaluator.shared.repository.PersonTagRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;
    private final PersonTagRepository personTagRepository;
    private final UserRepository userRepository;
    private final StatsCacheService statsCacheService;

    public ActivityLogController(ActivityLogRepository activityLogRepository,
                                 PersonTagRepository personTagRepository,
                                 UserRepository userRepository,
                                 StatsCacheService statsCacheService) {
        this.activityLogRepository = activityLogRepository;
        this.personTagRepository = personTagRepository;
        this.userRepository = userRepository;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, List<String>>> getSuggestions(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        migratePersonTagsIfNeeded(userId);
        List<PersonTag> tags = personTagRepository.findByUserId(userId);
        List<String> persons = tags.stream()
                .map(PersonTag::getName)
                .sorted()
                .collect(Collectors.toList());
        Map<String, List<String>> suggestions = new HashMap<>();
        suggestions.put("persons", persons);
        suggestions.put("locations", activityLogRepository.findDistinctLocationsByUserId(userId));
        suggestions.put("activities", activityLogRepository.findDistinctActivitiesByUserId(userId));
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(activityLogRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody ActivityLog entry, HttpSession session) {
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
        entry.setPersonTags(resolvePersonTags(entry, user));
        ActivityLog saved = activityLogRepository.save(entry);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(@PathVariable String id, @RequestBody ActivityLog entry, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<ActivityLog> existingOpt = activityLogRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ActivityLog existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        entry.setId(id);
        entry.setUser(existing.getUser());
        entry.setCreatedAt(existing.getCreatedAt());
        entry.setPersonTags(resolvePersonTags(entry, existing.getUser()));
        return ResponseEntity.ok(activityLogRepository.save(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<ActivityLog> entryOpt = activityLogRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ActivityLog entry = entryOpt.get();
        if (entry.getUser() == null || !userId.equals(entry.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        activityLogRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    private Set<PersonTag> resolvePersonTags(ActivityLog entry, User user) {
        Set<PersonTag> tags = new HashSet<>();
        for (String person : entry.getPersonList()) {
            String trimmed = person.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String nameLower = trimmed.toLowerCase();
            Optional<PersonTag> existing = personTagRepository.findByNameLowerAndUserId(nameLower, user.getId());
            if (existing.isPresent()) {
                tags.add(existing.get());
            } else {
                PersonTag newTag = new PersonTag(trimmed);
                newTag.setUser(user);
                tags.add(personTagRepository.save(newTag));
            }
        }
        return tags;
    }

    private void migratePersonTagsIfNeeded(String userId) {
        List<ActivityLog> entries = activityLogRepository.findByUserId(userId);
        boolean needsMigration = entries.stream()
                .anyMatch(e -> e.getPersonTags() == null || e.getPersonTags().isEmpty());
        if (!needsMigration) {
            return;
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        personTagRepository.deleteEmptyTags(userId);
        for (ActivityLog entry : entries) {
            if (entry.getPersonTags() == null || entry.getPersonTags().isEmpty()) {
                entry.setPersonTags(resolvePersonTags(entry, user));
                activityLogRepository.save(entry);
            }
        }
    }
}
