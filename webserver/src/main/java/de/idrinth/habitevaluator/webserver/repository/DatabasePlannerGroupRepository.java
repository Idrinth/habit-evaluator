package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabasePlannerGroupRepository implements PlannerGroupRepository {

    private final JpaPlannerGroupRepository jpaRepository;

    public DatabasePlannerGroupRepository(JpaPlannerGroupRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PlannerGroup save(PlannerGroup group) {
        return jpaRepository.save(group);
    }

    @Override
    public Optional<PlannerGroup> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PlannerGroup> findAll() {
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
    public List<PlannerGroup> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
