package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.SleepEntry;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for sleep entry persistence.
 */
public interface SleepEntryRepository {

    SleepEntry save(SleepEntry entry);

    Optional<SleepEntry> findById(String id);

    List<SleepEntry> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<SleepEntry> findByUserId(String userId);
}
