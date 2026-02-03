package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class H2DiaryEntryRepository implements DiaryEntryRepository {

    @Override
    public DiaryEntry save(DiaryEntry entry) {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DiaryEntry existing = em.find(DiaryEntry.class, entry.getId());
            DiaryEntry result;
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
    public Optional<DiaryEntry> findById(String id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return Optional.ofNullable(em.find(DiaryEntry.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<DiaryEntry> findAll() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT d FROM DiaryEntry d", DiaryEntry.class).getResultList();
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
            DiaryEntry entry = em.find(DiaryEntry.class, id);
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
    public List<DiaryEntry> findByUserId(String userId) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT d FROM DiaryEntry d WHERE d.user.id = :userId", DiaryEntry.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
