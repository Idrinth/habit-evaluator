package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for meeting entry persistence.
 */
public interface MeetingEntryRepository {

    MeetingEntry save(MeetingEntry entry);

    Optional<MeetingEntry> findById(String id);

    List<MeetingEntry> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<MeetingEntry> findByUserId(String userId);

    List<String> findDistinctPlacesByUserId(String userId);
}
