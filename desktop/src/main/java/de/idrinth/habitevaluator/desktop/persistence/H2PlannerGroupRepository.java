package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;

import java.util.List;
import java.util.Optional;

public class H2PlannerGroupRepository implements PlannerGroupRepository {

    @Override
    public PlannerGroup save(PlannerGroup group) {
        return JpaTransactionHelper.saveOrUpdate(PlannerGroup.class, group, PlannerGroup::getId);
    }

    @Override
    public Optional<PlannerGroup> findById(String id) {
        return JpaTransactionHelper.findById(PlannerGroup.class, id);
    }

    @Override
    public List<PlannerGroup> findAll() {
        return JpaTransactionHelper.findAll(PlannerGroup.class, "PlannerGroup");
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(PlannerGroup.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(PlannerGroup.class, id);
    }

    @Override
    public List<PlannerGroup> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                PlannerGroup.class,
                "SELECT g FROM PlannerGroup g WHERE g.user.id = :userId ORDER BY g.name",
                "userId",
                userId);
    }
}
