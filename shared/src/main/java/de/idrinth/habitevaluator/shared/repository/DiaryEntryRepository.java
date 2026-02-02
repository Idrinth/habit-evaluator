package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for diary entry persistence.
 */
public interface DiaryEntryRepository {

    DiaryEntry save(DiaryEntry entry);

    Optional<DiaryEntry> findById(String id);

    List<DiaryEntry> findAll();

    void deleteById(String id);

    List<DiaryEntry> findByUserId(String userId);
}
