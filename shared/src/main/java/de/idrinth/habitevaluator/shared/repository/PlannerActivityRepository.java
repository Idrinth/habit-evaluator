package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for planner activity persistence.
 */
public interface PlannerActivityRepository {

    PlannerActivity save(PlannerActivity activity);

    Optional<PlannerActivity> findById(String id);

    List<PlannerActivity> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<PlannerActivity> findByUserId(String userId);

    List<PlannerActivity> findByGroupId(String groupId);
}
