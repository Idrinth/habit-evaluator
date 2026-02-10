package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository;

import java.util.List;
import java.util.Optional;

public class H2FoodTagRepository implements FoodTagRepository {

    @Override
    public FoodTag save(FoodTag tag) {
        return JpaTransactionHelper.saveOrUpdate(FoodTag.class, tag, FoodTag::getId);
    }

    @Override
    public Optional<FoodTag> findById(String id) {
        return JpaTransactionHelper.findById(FoodTag.class, id);
    }

    @Override
    public List<FoodTag> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                FoodTag.class,
                "SELECT t FROM FoodTag t WHERE t.user.id = :userId ORDER BY LOWER(t.name)",
                "userId",
                userId);
    }

    @Override
    public Optional<FoodTag> findByNameLowerAndUserId(String nameLower, String userId) {
        List<FoodTag> results = JpaTransactionHelper.executeReadOnly(em ->
                em.createQuery(
                                "SELECT t FROM FoodTag t WHERE t.nameLower = :nameLower AND t.user.id = :userId",
                                FoodTag.class)
                        .setParameter("nameLower", nameLower)
                        .setParameter("userId", userId)
                        .getResultList());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(FoodTag.class, id);
    }
}
