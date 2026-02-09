package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.MedicationLog;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for medication log entry persistence.
 */
public interface MedicationLogRepository {

    MedicationLog save(MedicationLog entry);

    Optional<MedicationLog> findById(String id);

    List<MedicationLog> findAll();

    void deleteById(String id);

    List<MedicationLog> findByUserId(String userId);
}
