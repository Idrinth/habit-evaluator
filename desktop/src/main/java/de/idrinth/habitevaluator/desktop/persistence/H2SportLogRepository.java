package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.SportLog;
import de.idrinth.habitevaluator.shared.repository.SportLogRepository;

import java.util.List;
import java.util.Optional;

public class H2SportLogRepository implements SportLogRepository {

    @Override
    public SportLog save(SportLog entry) {
        return JpaTransactionHelper.saveOrUpdate(SportLog.class, entry, SportLog::getId);
    }

    @Override
    public Optional<SportLog> findById(String id) {
        return JpaTransactionHelper.findById(SportLog.class, id);
    }

    @Override
    public List<SportLog> findAll() {
        return JpaTransactionHelper.findAll(SportLog.class, "SportLog");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(SportLog.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(SportLog.class, id);
    }

    @Override
    public List<SportLog> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                SportLog.class,
                "SELECT s FROM SportLog s WHERE s.user.id = :userId ORDER BY s.date DESC, s.startTime DESC",
                "userId",
                userId);
    }

    @Override
    public List<String> findDistinctNamesByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                String.class,
                "SELECT DISTINCT s.name FROM SportLog s WHERE s.user.id = :userId ORDER BY s.name",
                "userId",
                userId);
    }

    @Override
    public List<String> findDistinctMeasurementUnitsByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                String.class,
                "SELECT DISTINCT s.measurementUnit FROM SportLog s WHERE s.user.id = :userId ORDER BY s.measurementUnit",
                "userId",
                userId);
    }
}
