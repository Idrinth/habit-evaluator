package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.FoodLog;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for food log persistence.
 */
public interface FoodLogRepository {

    FoodLog save(FoodLog entry);

    Optional<FoodLog> findById(String id);

    List<FoodLog> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<FoodLog> findByUserId(String userId);
}
