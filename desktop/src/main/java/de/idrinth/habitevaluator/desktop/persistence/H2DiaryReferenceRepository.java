package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;

import java.util.List;
import java.util.Optional;

public class H2DiaryReferenceRepository implements DiaryReferenceRepository {

    @Override
    public DiaryReference save(DiaryReference reference) {
        return JpaTransactionHelper.saveOrUpdate(DiaryReference.class, reference, DiaryReference::getId);
    }

    @Override
    public Optional<DiaryReference> findById(String id) {
        return JpaTransactionHelper.findById(DiaryReference.class, id);
    }

    @Override
    public List<DiaryReference> findAll() {
        return JpaTransactionHelper.findAll(DiaryReference.class, "DiaryReference");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(DiaryReference.class, id);
    }

    @Override
    public List<DiaryReference> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                DiaryReference.class,
                "SELECT r FROM DiaryReference r WHERE r.user.id = :userId",
                "userId",
                userId);
    }

    @Override
    public Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(String userId, String description) {
        if (description == null) {
            return Optional.empty();
        }
        return JpaTransactionHelper.executeReadOnly(em -> {
            try {
                DiaryReference result = em.createQuery(
                                "SELECT r FROM DiaryReference r WHERE r.user.id = :userId AND r.descriptionLower = :descLower",
                                DiaryReference.class)
                        .setParameter("userId", userId)
                        .setParameter("descLower", description.toLowerCase())
                        .getSingleResult();
                return Optional.of(result);
            } catch (jakarta.persistence.NoResultException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                String.class,
                "SELECT DISTINCT r.description FROM DiaryReference r WHERE r.user.id = :userId ORDER BY r.description",
                "userId",
                userId);
    }
}
