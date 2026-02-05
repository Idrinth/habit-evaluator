package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;

import java.util.List;
import java.util.Optional;

/**
 * H2 database implementation of HabitRepository using JPA.
 * Used by the desktop application for local persistence.
 */
public class H2HabitRepository implements HabitRepository {

    @Override
    public Habit save(Habit habit) {
        return JpaTransactionHelper.saveOrUpdate(Habit.class, habit, Habit::getId);
    }

    @Override
    public Optional<Habit> findById(String id) {
        return JpaTransactionHelper.findById(Habit.class, id);
    }

    @Override
    public List<Habit> findAll() {
        return JpaTransactionHelper.findAll(Habit.class, "Habit");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(Habit.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(Habit.class, id);
    }

    @Override
    public List<Habit> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                Habit.class,
                "SELECT h FROM Habit h WHERE h.user.id = :userId",
                "userId",
                userId);
    }
}
