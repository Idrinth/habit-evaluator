package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for gratitude entry persistence.
 */
public interface GratitudeEntryRepository {

    GratitudeEntry save(GratitudeEntry entry);

    Optional<GratitudeEntry> findById(String id);

    List<GratitudeEntry> findAll();

    void deleteById(String id);

    List<GratitudeEntry> findByUserId(String userId);
}
