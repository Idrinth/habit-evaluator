package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.ActivityGroup;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for activity group persistence.
 */
public interface ActivityGroupRepository {

    ActivityGroup save(ActivityGroup group);

    Optional<ActivityGroup> findById(String id);

    List<ActivityGroup> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<ActivityGroup> findByUserId(String userId);
}
