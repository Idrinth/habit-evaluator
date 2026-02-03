package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.EmotionPair;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for emotion pair persistence.
 */
public interface EmotionPairRepository {

    EmotionPair save(EmotionPair pair);

    Optional<EmotionPair> findById(String id);

    List<EmotionPair> findAll();

    void deleteById(String id);

    List<EmotionPair> findByUserId(String userId);
}
