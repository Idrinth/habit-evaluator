package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for emergency plan step persistence.
 */
public interface EmergencyPlanStepRepository {

    EmergencyPlanStep save(EmergencyPlanStep step);

    Optional<EmergencyPlanStep> findById(String id);

    List<EmergencyPlanStep> findAll();

    void deleteById(String id);

    List<EmergencyPlanStep> findByUserId(String userId);
}
