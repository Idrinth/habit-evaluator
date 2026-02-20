package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.ActivityLog;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for activity log persistence.
 */
public interface ActivityLogRepository {

    ActivityLog save(ActivityLog entry);

    Optional<ActivityLog> findById(String id);

    List<ActivityLog> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<ActivityLog> findByUserId(String userId);

    List<String> findDistinctLocationsByUserId(String userId);

    List<String> findDistinctActivitiesByUserId(String userId);
}
