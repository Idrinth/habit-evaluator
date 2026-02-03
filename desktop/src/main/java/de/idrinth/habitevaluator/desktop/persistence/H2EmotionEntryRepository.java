package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class H2EmotionEntryRepository implements EmotionEntryRepository {

    @Override
    public EmotionEntry save(EmotionEntry entry) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EmotionEntry existing = em.find(EmotionEntry.class, entry.getId());
            EmotionEntry result;
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
    public Optional<EmotionEntry> findById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            EmotionEntry entry = em.find(EmotionEntry.class, id);
            return Optional.ofNullable(entry);
        } finally {
            em.close();
        }
    }

    @Override
    public List<EmotionEntry> findAll() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM EmotionEntry e", EmotionEntry.class)
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
            EmotionEntry entry = em.find(EmotionEntry.class, id);
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
    public List<EmotionEntry> findByUserId(String userId) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM EmotionEntry e WHERE e.user.id = :userId", EmotionEntry.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
