package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaScoringRuleRepositoryTest {

    @Autowired
    private JpaScoringRuleRepository scoringRuleRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("ruleuser", "password123", "ruleuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        ScoringRule rule = new ScoringRule("Default");
        rule.setUser(testUser);
        ScoringRule saved = scoringRuleRepository.save(rule);

        Optional<ScoringRule> found = scoringRuleRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Default", found.get().getName());
        assertEquals(1, found.get().getThresholdFor1Point());
        assertEquals(2, found.get().getThresholdFor2Points());
        assertEquals(4, found.get().getThresholdFor4Points());
        assertEquals(7, found.get().getThresholdFor8Points());
    }

    @Test
    void testSaveWithCustomThresholds() {
        ScoringRule rule = new ScoringRule("Custom", 2, 4, 6, 10);
        rule.setUser(testUser);
        ScoringRule saved = scoringRuleRepository.save(rule);

        Optional<ScoringRule> found = scoringRuleRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(2, found.get().getThresholdFor1Point());
        assertEquals(4, found.get().getThresholdFor2Points());
        assertEquals(6, found.get().getThresholdFor4Points());
        assertEquals(10, found.get().getThresholdFor8Points());
    }

    @Test
    void testFindByUserId() {
        ScoringRule rule1 = new ScoringRule("Rule A");
        rule1.setUser(testUser);
        scoringRuleRepository.save(rule1);

        ScoringRule rule2 = new ScoringRule("Rule B");
        rule2.setUser(testUser);
        scoringRuleRepository.save(rule2);

        List<ScoringRule> rules = scoringRuleRepository.findByUserId(testUser.getId());

        assertEquals(2, rules.size());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        ScoringRule rule = new ScoringRule("Default");
        rule.setUser(testUser);
        scoringRuleRepository.save(rule);

        User otherUser = new User("otherruleuser", "password", "otherrule@example.com");
        otherUser = userRepository.save(otherUser);

        List<ScoringRule> rules = scoringRuleRepository.findByUserId(otherUser.getId());
        assertTrue(rules.isEmpty());
    }

    @Test
    void testDeleteById() {
        ScoringRule rule = new ScoringRule("Default");
        rule.setUser(testUser);
        ScoringRule saved = scoringRuleRepository.save(rule);

        scoringRuleRepository.deleteById(saved.getId());

        assertFalse(scoringRuleRepository.findById(saved.getId()).isPresent());
    }
}
