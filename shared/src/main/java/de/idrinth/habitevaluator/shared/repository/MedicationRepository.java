package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.Medication;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for medication base data persistence.
 */
public interface MedicationRepository {

    Medication save(Medication medication);

    Optional<Medication> findById(String id);

    List<Medication> findAll();

    void deleteById(String id);

    List<Medication> findByUserId(String userId);
}
