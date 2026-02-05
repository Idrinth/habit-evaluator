package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.ScoringRuleRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScoringRuleControllerTest {

    private ScoringRuleRepository scoringRuleRepository;
    private UserRepository userRepository;
    private ScoringRuleController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        scoringRuleRepository = mock(ScoringRuleRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new ScoringRuleController(scoringRuleRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testCreateScoringRuleSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(scoringRuleRepository.save(any(ScoringRule.class))).thenAnswer(i -> i.getArgument(0));

        ScoringRuleController.CreateScoringRuleRequest request = new ScoringRuleController.CreateScoringRuleRequest();
        request.name = "Default Rule";
        request.thresholdFor1Point = 1;
        request.thresholdFor2Points = 2;
        request.thresholdFor4Points = 4;
        request.thresholdFor8Points = 7;

        ResponseEntity<?> response = controller.createScoringRule(request, session);

        assertEquals(200, response.getStatusCode().value());
        ScoringRule rule = (ScoringRule) response.getBody();
        assertEquals("Default Rule", rule.getName());
    }

    @Test
    void testCreateScoringRuleInvalidThresholds() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        ScoringRuleController.CreateScoringRuleRequest request = new ScoringRuleController.CreateScoringRuleRequest();
        request.name = "Bad Rule";
        request.thresholdFor1Point = 5;
        request.thresholdFor2Points = 3;
        request.thresholdFor4Points = 2;
        request.thresholdFor8Points = 1;

        ResponseEntity<?> response = controller.createScoringRule(request, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateScoringRuleUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ScoringRuleController.CreateScoringRuleRequest request = new ScoringRuleController.CreateScoringRuleRequest();
        request.name = "Test";

        ResponseEntity<?> response = controller.createScoringRule(request, unauthSession);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateScoringRuleUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        ScoringRuleController.CreateScoringRuleRequest request = new ScoringRuleController.CreateScoringRuleRequest();
        request.name = "Test";

        ResponseEntity<?> response = controller.createScoringRule(request, session);

        assertEquals(401, response.getStatusCode().value());
    }
}
