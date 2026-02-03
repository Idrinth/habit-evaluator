package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class H2EmotionPairRepository implements EmotionPairRepository {

    @Override
    public EmotionPair save(EmotionPair pair) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EmotionPair existing = em.find(EmotionPair.class, pair.getId());
            EmotionPair result;
            if (existing != null) {
                result = em.merge(pair);
            } else {
                em.persist(pair);
                result = pair;
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
    public Optional<EmotionPair> findById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            EmotionPair pair = em.find(EmotionPair.class, id);
            return Optional.ofNullable(pair);
        } finally {
            em.close();
        }
    }

    @Override
    public List<EmotionPair> findAll() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM EmotionPair e", EmotionPair.class)
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
            EmotionPair pair = em.find(EmotionPair.class, id);
            if (pair != null) {
                em.remove(pair);
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
    public List<EmotionPair> findByUserId(String userId) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM EmotionPair e WHERE e.user.id = :userId", EmotionPair.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
