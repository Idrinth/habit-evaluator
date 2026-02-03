package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class H2SleepEntryRepository implements SleepEntryRepository {

    @Override
    public SleepEntry save(SleepEntry entry) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SleepEntry existing = em.find(SleepEntry.class, entry.getId());
            SleepEntry result;
            if (existing != null) {
                result = em.merge(entry);
            } else {
                em.persist(entry);
                result = entry;
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
    public Optional<SleepEntry> findById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            SleepEntry entry = em.find(SleepEntry.class, id);
            return Optional.ofNullable(entry);
        } finally {
            em.close();
        }
    }

    @Override
    public List<SleepEntry> findAll() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM SleepEntry s", SleepEntry.class)
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
            SleepEntry entry = em.find(SleepEntry.class, id);
            if (entry != null) {
                em.remove(entry);
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
            return em.find(SleepEntry.class, id) != null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<SleepEntry> findByUserId(String userId) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM SleepEntry s WHERE s.user.id = :userId", SleepEntry.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
