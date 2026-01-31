package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/init-defaults")
public class DefaultDataController {

    private final HabitCategoryRepository categoryRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public DefaultDataController(HabitCategoryRepository categoryRepository,
                                 HabitRepository habitRepository,
                                 UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> initializeDefaults(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        DefaultDataInitializer initializer = new DefaultDataInitializer(categoryRepository, habitRepository);
        initializer.initializeDefaults(userOpt.get());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
