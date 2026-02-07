package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.SportLog;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for sport log persistence.
 */
public interface SportLogRepository {

    SportLog save(SportLog entry);

    Optional<SportLog> findById(String id);

    List<SportLog> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<SportLog> findByUserId(String userId);
}
