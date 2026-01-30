package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.repository.ScoringRuleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of ScoringRuleRepository.
 * Uses Spring Data JPA for persistence operations.
 */
@Repository
public class DatabaseScoringRuleRepository implements ScoringRuleRepository {

    private final JpaScoringRuleRepository jpaRepository;

    public DatabaseScoringRuleRepository(JpaScoringRuleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ScoringRule save(ScoringRule scoringRule) {
        return jpaRepository.save(scoringRule);
    }

    @Override
    public Optional<ScoringRule> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ScoringRule> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ScoringRule> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
