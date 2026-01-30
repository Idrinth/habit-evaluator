package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.ScoringRuleRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controller for serving HTML pages.
 */
@Controller
public class PageController {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final PasswordEncoder passwordEncoder;
    private final HabitCategoryRepository habitCategoryRepository;
    private final ScoringRuleRepository scoringRuleRepository;

    public PageController(UserRepository userRepository, HabitRepository habitRepository,
                          PasswordEncoder passwordEncoder, HabitCategoryRepository habitCategoryRepository,
                          ScoringRuleRepository scoringRuleRepository) {
        this.userRepository = userRepository;
        this.habitRepository = habitRepository;
        this.passwordEncoder = passwordEncoder;
        this.habitCategoryRepository = habitCategoryRepository;
        this.scoringRuleRepository = scoringRuleRepository;
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

    @GetMapping("/habits/add")
    public String addHabitPage(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        return "add-habit";
    }

    @PostMapping("/habits/add")
    public String addHabit(@RequestParam String name,
                           @RequestParam(required = false) String description,
                           @RequestParam FrequencyType frequencyType,
                           @RequestParam int targetFrequency,
                           HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        Habit habit = new Habit(name, description);
        habit.setFrequencyType(frequencyType);
        habit.setTargetFrequency(targetFrequency);
        habit.setUser(userOpt.get());
        habitRepository.save(habit);
        model.addAttribute("success", "Habit \"" + name + "\" created successfully");
        return "add-habit";
    }

    @GetMapping("/score-rules/add")
    public String addScoringRulePage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        return "score-rule-add";
    }

    @PostMapping("/score-rules/add")
    public String addScoringRule(@RequestParam String name,
                                 @RequestParam int thresholdFor1Point,
                                 @RequestParam int thresholdFor2Points,
                                 @RequestParam int thresholdFor4Points,
                                 @RequestParam int thresholdFor8Points,
                                 HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        try {
            ScoringRule rule = new ScoringRule(name, thresholdFor1Point, thresholdFor2Points,
                    thresholdFor4Points, thresholdFor8Points);
            rule.setUser(userOpt.get());
            scoringRuleRepository.save(rule);
            model.addAttribute("success", "Scoring rule created successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "score-rule-add";
    }

    @GetMapping("/habits/track")
    public String trackHabitsPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        List<Habit> habits = habitRepository.findByUserId(userId);
        model.addAttribute("habits", habits);
        return "track-habits";
    }

    @PostMapping("/habits/track")
    public String trackHabits(@RequestParam(required = false) List<String> habitIds,
                              HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if (habitIds == null || habitIds.isEmpty()) {
            List<Habit> habits = habitRepository.findByUserId(userId);
            model.addAttribute("habits", habits);
            model.addAttribute("error", "No habits were selected");
            return "track-habits";
        }
        int count = 0;
        for (String habitId : habitIds) {
            Optional<Habit> habitOpt = habitRepository.findById(habitId);
            if (habitOpt.isPresent()) {
                Habit habit = habitOpt.get();
                if (habit.getUser() != null && userId.equals(habit.getUser().getId())) {
                    HabitEntry entry = new HabitEntry();
                    entry.setHabit(habit);
                    habit.addEntry(entry);
                    habitRepository.save(habit);
                    count++;
                }
            }
        }
        List<Habit> habits = habitRepository.findByUserId(userId);
        model.addAttribute("habits", habits);
        model.addAttribute("success", count + " habit(s) tracked successfully");
        return "track-habits";
    }
}
