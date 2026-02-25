package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/food-logs")
public class FoodLogController {

    private final FoodLogRepository foodLogRepository;
    private final FoodTagRepository foodTagRepository;
    private final UserRepository userRepository;
    private final StatsCacheService statsCacheService;

    public FoodLogController(FoodLogRepository foodLogRepository,
                             FoodTagRepository foodTagRepository,
                             UserRepository userRepository,
                             StatsCacheService statsCacheService) {
        this.foodLogRepository = foodLogRepository;
        this.foodTagRepository = foodTagRepository;
        this.userRepository = userRepository;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping
    public ResponseEntity<List<FoodLog>> getAllEntries(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(foodLogRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody FoodLog entry, HttpSession session) {
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
        entry.setTags(resolveTagsFromFoodItems(entry, user));
        FoodLog saved = foodLogRepository.save(entry);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<FoodLog> entryOpt = foodLogRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FoodLog entry = entryOpt.get();
        if (entry.getUser() == null || !userId.equals(entry.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        foodLogRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        migrateTagsIfNeeded(userId);
        List<FoodTag> tags = foodTagRepository.findByUserId(userId);
        List<String> suggestions = tags.stream()
                .map(FoodTag::getName)
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(suggestions);
    }

    private void migrateTagsIfNeeded(String userId) {
        List<FoodLog> entries = foodLogRepository.findByUserId(userId);
        boolean needsMigration = entries.stream()
                .anyMatch(e -> e.getTags() == null || e.getTags().isEmpty());
        if (!needsMigration) {
            return;
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        foodTagRepository.deleteEmptyTags(userId);
        for (FoodLog entry : entries) {
            if (entry.getTags() == null || entry.getTags().isEmpty()) {
                entry.setTags(resolveTagsFromFoodItems(entry, user));
                foodLogRepository.save(entry);
            }
        }
    }

    @PostMapping("/migrate-tags")
    public ResponseEntity<Void> migrateTags(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        foodTagRepository.deleteEmptyTags(userId);
        List<FoodLog> entries = foodLogRepository.findByUserId(userId);
        for (FoodLog entry : entries) {
            if (entry.getTags() == null || entry.getTags().isEmpty()) {
                entry.setTags(resolveTagsFromFoodItems(entry, user));
                foodLogRepository.save(entry);
            }
        }
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    private Set<FoodTag> resolveTagsFromFoodItems(FoodLog entry, User user) {
        Set<FoodTag> tags = new HashSet<>();
        for (String item : entry.getFoodItemList()) {
            String trimmed = item.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String nameLower = trimmed.toLowerCase();
            Optional<FoodTag> existing = foodTagRepository.findByNameLowerAndUserId(nameLower, user.getId());
            if (existing.isPresent()) {
                tags.add(existing.get());
            } else {
                FoodTag newTag = new FoodTag(trimmed);
                newTag.setUser(user);
                tags.add(foodTagRepository.save(newTag));
            }
        }
        return tags;
    }
}
