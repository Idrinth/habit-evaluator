package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of HabitRepository.
 * Uses Spring Data JPA for persistence operations.
 */
@Repository
public class DatabaseHabitRepository implements HabitRepository {

    private final JpaHabitRepository jpaRepository;

    public DatabaseHabitRepository(JpaHabitRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Habit save(Habit habit) {
        return jpaRepository.save(habit);
    }

    @Override
    public Optional<Habit> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Habit> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Habit> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
