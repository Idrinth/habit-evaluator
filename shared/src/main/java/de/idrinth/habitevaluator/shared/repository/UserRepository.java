package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for user persistence.
 * Platform-specific implementations will handle the actual storage.
 */
public interface UserRepository {

    /**
     * Saves a user.
     *
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Finds a user by their ID.
     *
     * @param id the user ID
     * @return the user if found
     */
    Optional<User> findById(String id);

    /**
     * Finds a user by their username.
     *
     * @param username the username
     * @return the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email.
     *
     * @param email the email
     * @return the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all users.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Deletes a user by their ID.
     *
     * @param id the user ID to delete
     */
    void deleteById(String id);

    /**
     * Checks if a user exists with the given ID.
     *
     * @param id the user ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username the username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);
}
