package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final HabitCategoryRepository habitCategoryRepository;

    public CategoryController(HabitCategoryRepository habitCategoryRepository) {
        this.habitCategoryRepository = habitCategoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<HabitCategory>> listCategories(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(habitCategoryRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<HabitCategory> createCategory(@RequestBody CreateCategoryRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        HabitCategory category = new HabitCategory(request.name, request.description, request.color);
        return ResponseEntity.ok(habitCategoryRepository.save(category));
    }

    public static class CreateCategoryRequest {
        public String name;
        public String description;
        public String color;
    }
}
