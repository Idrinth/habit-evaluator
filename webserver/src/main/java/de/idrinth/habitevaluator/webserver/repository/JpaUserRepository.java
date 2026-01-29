package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entities.
 * Provides database-backed persistence for users.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
}
