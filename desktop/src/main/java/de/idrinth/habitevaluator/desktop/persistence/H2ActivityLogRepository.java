package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;

import java.util.List;
import java.util.Optional;

public class H2ActivityLogRepository implements ActivityLogRepository {

    @Override
    public ActivityLog save(ActivityLog entry) {
        return JpaTransactionHelper.saveOrUpdate(ActivityLog.class, entry, ActivityLog::getId);
    }

    @Override
    public Optional<ActivityLog> findById(String id) {
        return JpaTransactionHelper.findById(ActivityLog.class, id);
    }

    @Override
    public List<ActivityLog> findAll() {
        return JpaTransactionHelper.findAll(ActivityLog.class, "ActivityLog");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(ActivityLog.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(ActivityLog.class, id);
    }

    @Override
    public List<ActivityLog> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                ActivityLog.class,
                "SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.date DESC, a.startTime DESC",
                "userId",
                userId);
    }
}
