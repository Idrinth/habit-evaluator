package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final HabitCategoryRepository habitCategoryRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final StatsCacheService statsCacheService;

    public CategoryController(HabitCategoryRepository habitCategoryRepository, HabitRepository habitRepository,
                              UserRepository userRepository, StatsCacheService statsCacheService) {
        this.habitCategoryRepository = habitCategoryRepository;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.statsCacheService = statsCacheService;
    }

    @GetMapping
    public ResponseEntity<List<HabitCategory>> listCategories(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(habitCategoryRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<HabitCategory> createCategory(@RequestBody CreateCategoryRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        HabitCategory category = new HabitCategory(request.name, request.description, request.color);
        category.setUser(userOpt.get());
        return ResponseEntity.ok(habitCategoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitCategory> updateCategory(@PathVariable String id, @RequestBody CreateCategoryRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<HabitCategory> existingOpt = habitCategoryRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HabitCategory existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        existing.setName(request.name);
        existing.setDescription(request.description);
        existing.setColor(request.color);
        return ResponseEntity.ok(habitCategoryRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable String id,
            @RequestParam(required = false) String reassignTo,
            @RequestParam(defaultValue = "false") boolean confirm,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<HabitCategory> existingOpt = habitCategoryRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HabitCategory existing = existingOpt.get();
        if (existing.getUser() == null || !userId.equals(existing.getUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        List<Habit> affectedHabits = habitRepository.findByUserId(userId).stream()
                .filter(h -> id.equals(h.getCategoryId()))
                .toList();
        if (reassignTo != null && !reassignTo.isBlank()) {
            Optional<HabitCategory> targetOpt = habitCategoryRepository.findById(reassignTo);
            if (targetOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            HabitCategory target = targetOpt.get();
            if (target.getUser() == null || !userId.equals(target.getUser().getId())) {
                return ResponseEntity.badRequest().build();
            }
            for (Habit habit : affectedHabits) {
                habit.setCategoryId(reassignTo);
                habitRepository.save(habit);
            }
        } else if (!affectedHabits.isEmpty()) {
            if (!confirm) {
                return ResponseEntity.status(409).build();
            }
            for (Habit habit : affectedHabits) {
                habitRepository.deleteById(habit.getId());
            }
        }
        habitCategoryRepository.deleteById(id);
        statsCacheService.invalidateUser(userId);
        return ResponseEntity.noContent().build();
    }

    public static class CreateCategoryRequest {
        public String name;
        public String description;
        public String color;
    }
}
