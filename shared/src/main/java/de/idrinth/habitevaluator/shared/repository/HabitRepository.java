package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.Habit;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for habit persistence.
 * Platform-specific implementations will handle the actual storage.
 */
public interface HabitRepository {

    /**
     * Saves a habit.
     *
     * @param habit the habit to save
     * @return the saved habit
     */
    Habit save(Habit habit);

    /**
     * Finds a habit by its ID.
     *
     * @param id the habit ID
     * @return the habit if found
     */
    Optional<Habit> findById(String id);

    /**
     * Retrieves all habits.
     *
     * @return list of all habits
     */
    List<Habit> findAll();

    /**
     * Deletes a habit by its ID.
     *
     * @param id the habit ID to delete
     */
    void deleteById(String id);

    /**
     * Checks if a habit exists with the given ID.
     *
     * @param id the habit ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Finds all habits for a specific user.
     *
     * @param userId the user ID
     * @return list of habits for the user
     */
    List<Habit> findByUserId(String userId);
}
