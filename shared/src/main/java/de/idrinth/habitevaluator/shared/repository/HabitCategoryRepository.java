package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.HabitCategory;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for habit category persistence.
 * Platform-specific implementations will handle the actual storage.
 */
public interface HabitCategoryRepository {

    /**
     * Saves a habit category.
     *
     * @param category the category to save
     * @return the saved category
     */
    HabitCategory save(HabitCategory category);

    /**
     * Finds a habit category by its ID.
     *
     * @param id the category ID
     * @return the category if found
     */
    Optional<HabitCategory> findById(String id);

    /**
     * Retrieves all habit categories.
     *
     * @return list of all categories
     */
    List<HabitCategory> findAll();

    /**
     * Deletes a habit category by its ID.
     *
     * @param id the category ID to delete
     */
    void deleteById(String id);

    /**
     * Checks if a habit category exists with the given ID.
     *
     * @param id the category ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);
}
