package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for emotion entry persistence.
 */
public interface EmotionEntryRepository {

    EmotionEntry save(EmotionEntry entry);

    Optional<EmotionEntry> findById(String id);

    List<EmotionEntry> findAll();

    void deleteById(String id);

    List<EmotionEntry> findByUserId(String userId);
}
