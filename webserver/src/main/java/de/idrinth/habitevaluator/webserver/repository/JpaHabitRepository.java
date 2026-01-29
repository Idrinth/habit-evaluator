package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Habit entities.
 * Provides database-backed persistence for habits.
 */
@Repository
public interface JpaHabitRepository extends JpaRepository<Habit, String> {
}
