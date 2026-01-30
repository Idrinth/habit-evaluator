package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Controller for serving HTML pages.
 */
@Controller
public class PageController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HabitCategoryRepository habitCategoryRepository;

    public PageController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          HabitCategoryRepository habitCategoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.habitCategoryRepository = habitCategoryRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        User user = userOpt.get();
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        return "redirect:/login";
    }

    @GetMapping("/categories/add")
    public String addCategoryPage() {
        return "add-category";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String color,
                              Model model) {
        if (name == null || name.isBlank()) {
            model.addAttribute("error", "Category name is required");
            return "add-category";
        }

        HabitCategory category = new HabitCategory(name, description, color);
        habitCategoryRepository.save(category);

        model.addAttribute("success", "Category created successfully");
        return "add-category";
    }
}
