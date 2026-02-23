package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.SlotConfirmation;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for slot confirmation persistence.
 */
public interface SlotConfirmationRepository {

    SlotConfirmation save(SlotConfirmation confirmation);

    Optional<SlotConfirmation> findById(String id);

    List<SlotConfirmation> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<SlotConfirmation> findByUserId(String userId);
}
