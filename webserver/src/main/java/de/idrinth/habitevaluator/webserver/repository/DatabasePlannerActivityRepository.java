package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabasePlannerActivityRepository implements PlannerActivityRepository {

    private final JpaPlannerActivityRepository jpaRepository;

    public DatabasePlannerActivityRepository(JpaPlannerActivityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PlannerActivity save(PlannerActivity activity) {
        return jpaRepository.save(activity);
    }

    @Override
    public Optional<PlannerActivity> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PlannerActivity> findAll() {
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
    public List<PlannerActivity> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<PlannerActivity> findByGroupId(String groupId) {
        return jpaRepository.findByGroupId(groupId);
    }
}
