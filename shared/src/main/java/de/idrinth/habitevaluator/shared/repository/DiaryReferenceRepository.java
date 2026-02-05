package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.DiaryReference;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for diary reference persistence.
 */
public interface DiaryReferenceRepository {

    DiaryReference save(DiaryReference reference);

    Optional<DiaryReference> findById(String id);

    List<DiaryReference> findAll();

    void deleteById(String id);

    List<DiaryReference> findByUserId(String userId);

    /**
     * Finds a reference by user and description (case-insensitive).
     * This is the primary lookup method for finding existing references.
     */
    Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(String userId, String description);

    /**
     * Gets all distinct descriptions for a user.
     */
    List<String> findDistinctDescriptionsByUserId(String userId);

    /**
     * Finds or creates a reference for the given description (case-insensitive matching).
     * If a reference with the same description (case-insensitive) exists for the user,
     * it is returned. Otherwise, a new reference is created with the original case preserved.
     */
    default DiaryReference findOrCreate(String userId, String description, java.util.function.Supplier<de.idrinth.habitevaluator.shared.model.User> userSupplier) {
        if (description == null || description.isEmpty()) {
            return null;
        }
        return findByUserIdAndDescriptionIgnoreCase(userId, description)
                .orElseGet(() -> {
                    DiaryReference ref = new DiaryReference(description);
                    ref.setUser(userSupplier.get());
                    return save(ref);
                });
    }
}
