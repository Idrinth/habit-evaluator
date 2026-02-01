package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ScoringRuleRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/score-rules")
public class ScoringRuleController {

    private final ScoringRuleRepository scoringRuleRepository;
    private final UserRepository userRepository;

    public ScoringRuleController(ScoringRuleRepository scoringRuleRepository, UserRepository userRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createScoringRule(@RequestBody CreateScoringRuleRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            ScoringRule rule = new ScoringRule(
                    request.name,
                    request.thresholdFor1Point,
                    request.thresholdFor2Points,
                    request.thresholdFor4Points,
                    request.thresholdFor8Points
            );
            rule.setUser(userOpt.get());
            return ResponseEntity.ok(scoringRuleRepository.save(rule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("message", e.getMessage()));
        }
    }

    public static class CreateScoringRuleRequest {
        public String name;
        public int thresholdFor1Point;
        public int thresholdFor2Points;
        public int thresholdFor4Points;
        public int thresholdFor8Points;
    }
}
