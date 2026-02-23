package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for planner group persistence.
 */
public interface PlannerGroupRepository {

    PlannerGroup save(PlannerGroup group);

    Optional<PlannerGroup> findById(String id);

    List<PlannerGroup> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<PlannerGroup> findByUserId(String userId);
}
