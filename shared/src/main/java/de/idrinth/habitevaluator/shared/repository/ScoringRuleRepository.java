package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.ScoringRule;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for scoring rule persistence.
 * Platform-specific implementations will handle the actual storage.
 */
public interface ScoringRuleRepository {

    ScoringRule save(ScoringRule scoringRule);

    Optional<ScoringRule> findById(String id);

    List<ScoringRule> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<ScoringRule> findByUserId(String userId);
}
