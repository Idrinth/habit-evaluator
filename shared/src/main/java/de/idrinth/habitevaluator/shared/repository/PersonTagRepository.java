package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.PersonTag;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for person tag persistence.
 */
public interface PersonTagRepository {

    PersonTag save(PersonTag tag);

    Optional<PersonTag> findById(String id);

    List<PersonTag> findByUserId(String userId);

    Optional<PersonTag> findByNameLowerAndUserId(String nameLower, String userId);

    void deleteById(String id);

    void deleteEmptyTags(String userId);
}
