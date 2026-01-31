package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * H2 database implementation of HabitCategoryRepository using JPA.
 * Used by the desktop application for local persistence.
 */
public class H2HabitCategoryRepository implements HabitCategoryRepository {

    @Override
    public HabitCategory save(HabitCategory category) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            HabitCategory existing = em.find(HabitCategory.class, category.getId());
            HabitCategory result;
            if (existing != null) {
                result = em.merge(category);
            } else {
                em.persist(category);
                result = category;
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
    public Optional<HabitCategory> findById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            HabitCategory category = em.find(HabitCategory.class, id);
            return Optional.ofNullable(category);
        } finally {
            em.close();
        }
    }

    @Override
    public List<HabitCategory> findAll() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM HabitCategory c", HabitCategory.class)
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
            HabitCategory category = em.find(HabitCategory.class, id);
            if (category != null) {
                em.remove(category);
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
            return em.find(HabitCategory.class, id) != null;
        } finally {
            em.close();
        }
    }
}
