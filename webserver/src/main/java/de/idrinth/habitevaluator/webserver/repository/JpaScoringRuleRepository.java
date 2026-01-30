package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ScoringRule entities.
 */
@Repository
public interface JpaScoringRuleRepository extends JpaRepository<ScoringRule, String> {

    List<ScoringRule> findByUserId(String userId);
}
