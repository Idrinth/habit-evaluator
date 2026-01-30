package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ScoringRuleRepository;
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
    private final ScoringRuleRepository scoringRuleRepository;

    public PageController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          ScoringRuleRepository scoringRuleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
