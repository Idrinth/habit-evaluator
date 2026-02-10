package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * H2 database implementation of HabitCategoryRepository using JPA.
 * Used by the desktop application for local persistence.
 */
public class H2HabitCategoryRepository implements HabitCategoryRepository {

    @Override
    public HabitCategory save(HabitCategory category) {
        return JpaTransactionHelper.saveOrUpdate(HabitCategory.class, category, HabitCategory::getId);
    }

    @Override
    public Optional<HabitCategory> findById(String id) {
        return JpaTransactionHelper.findById(HabitCategory.class, id);
    }

    @Override
    public List<HabitCategory> findAll() {
        return JpaTransactionHelper.findAll(HabitCategory.class, "HabitCategory");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(HabitCategory.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(HabitCategory.class, id);
    }

    @Override
    public List<HabitCategory> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                HabitCategory.class,
                "SELECT c FROM HabitCategory c WHERE c.user.id = :userId ORDER BY LOWER(c.name)",
                "userId",
                userId);
    }
}
