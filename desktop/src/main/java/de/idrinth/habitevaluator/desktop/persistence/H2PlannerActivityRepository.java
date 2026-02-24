package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository;

import java.util.List;
import java.util.Optional;

public class H2PlannerActivityRepository implements PlannerActivityRepository {

    @Override
    public PlannerActivity save(PlannerActivity activity) {
        return JpaTransactionHelper.saveOrUpdate(PlannerActivity.class, activity, PlannerActivity::getId);
    }

    @Override
    public Optional<PlannerActivity> findById(String id) {
        return JpaTransactionHelper.findById(PlannerActivity.class, id);
    }

    @Override
    public List<PlannerActivity> findAll() {
        return JpaTransactionHelper.findAll(PlannerActivity.class, "PlannerActivity");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(PlannerActivity.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(PlannerActivity.class, id);
    }

    @Override
    public List<PlannerActivity> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                PlannerActivity.class,
                "SELECT a FROM PlannerActivity a WHERE a.user.id = :userId ORDER BY a.name",
                "userId",
                userId);
    }

    @Override
    public List<PlannerActivity> findByGroupId(String groupId) {
        return JpaTransactionHelper.findByParameter(
                PlannerActivity.class,
                "SELECT a FROM PlannerActivity a JOIN a.groups g WHERE g.id = :groupId ORDER BY a.name",
                "groupId",
                groupId);
    }
}
