package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseActivityLogRepository implements ActivityLogRepository {

    private final JpaActivityLogRepository jpaRepository;

    public DatabaseActivityLogRepository(JpaActivityLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ActivityLog save(ActivityLog entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<ActivityLog> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ActivityLog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ActivityLog> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<String> findDistinctLocationsByUserId(String userId) {
        return jpaRepository.findDistinctLocationsByUserId(userId);
    }

    @Override
    public List<String> findDistinctActivitiesByUserId(String userId) {
        return jpaRepository.findDistinctActivitiesByUserId(userId);
    }
}
