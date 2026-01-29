package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * H2 database implementation of HabitRepository using JPA.
 * Used by the desktop application for local persistence.
 */
public class H2HabitRepository implements HabitRepository {

    @Override
    public Habit save(Habit habit) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Habit existingHabit = em.find(Habit.class, habit.getId());
            Habit result;
            if (existingHabit != null) {
                result = em.merge(habit);
            } else {
                em.persist(habit);
                result = habit;
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Habit> findById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            Habit habit = em.find(Habit.class, id);
            return Optional.ofNullable(habit);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Habit> findAll() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT h FROM Habit h", Habit.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Habit habit = em.find(Habit.class, id);
            if (habit != null) {
                em.remove(habit);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.find(Habit.class, id) != null;
        } finally {
            em.close();
        }
    }
}
