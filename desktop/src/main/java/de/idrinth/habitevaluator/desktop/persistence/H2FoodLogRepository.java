package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository;

import java.util.List;
import java.util.Optional;

public class H2FoodLogRepository implements FoodLogRepository {

    @Override
    public FoodLog save(FoodLog entry) {
        return JpaTransactionHelper.saveOrUpdate(FoodLog.class, entry, FoodLog::getId);
    }

    @Override
    public Optional<FoodLog> findById(String id) {
        return JpaTransactionHelper.findById(FoodLog.class, id);
    }

    @Override
    public List<FoodLog> findAll() {
        return JpaTransactionHelper.findAll(FoodLog.class, "FoodLog");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(FoodLog.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(FoodLog.class, id);
    }

    @Override
    public List<FoodLog> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                FoodLog.class,
                "SELECT f FROM FoodLog f WHERE f.user.id = :userId",
                "userId",
                userId);
    }
}
