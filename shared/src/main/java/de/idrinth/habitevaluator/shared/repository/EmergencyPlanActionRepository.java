package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for emergency plan action persistence.
 */
public interface EmergencyPlanActionRepository {

    EmergencyPlanAction save(EmergencyPlanAction action);

    void saveAll(List<EmergencyPlanAction> actions);

    Optional<EmergencyPlanAction> findById(String id);

    List<EmergencyPlanAction> findByStepId(String stepId);

    void deleteById(String id);

    void deleteByStepId(String stepId);
}
