package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.FoodTag;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for food tag persistence.
 */
public interface FoodTagRepository {

    FoodTag save(FoodTag tag);

    Optional<FoodTag> findById(String id);

    List<FoodTag> findByUserId(String userId);

    Optional<FoodTag> findByNameLowerAndUserId(String nameLower, String userId);

    void deleteById(String id);
}
