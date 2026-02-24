package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class H2WeekPlannerSlotRepository implements WeekPlannerSlotRepository {

    @Override
    public WeekPlannerSlot save(WeekPlannerSlot slot) {
        return JpaTransactionHelper.saveOrUpdate(WeekPlannerSlot.class, slot, WeekPlannerSlot::getId);
    }

    @Override
    public Optional<WeekPlannerSlot> findById(String id) {
        return JpaTransactionHelper.findSingleByParameter(
                WeekPlannerSlot.class,
                "SELECT s FROM WeekPlannerSlot s LEFT JOIN FETCH s.groups WHERE s.id = :id",
                "id",
                id);
    }

    @Override
    public List<WeekPlannerSlot> findAll() {
        return JpaTransactionHelper.executeReadOnly(em ->
                em.createQuery("SELECT DISTINCT s FROM WeekPlannerSlot s LEFT JOIN FETCH s.groups", WeekPlannerSlot.class)
                        .getResultList());
    }

    @Override
    public void deleteById(String id) {
        JpaTransactionHelper.deleteById(WeekPlannerSlot.class, id);
    }

    @Override
    public boolean existsById(String id) {
        return JpaTransactionHelper.existsById(WeekPlannerSlot.class, id);
    }

    @Override
    public List<WeekPlannerSlot> findByUserId(String userId) {
        return JpaTransactionHelper.findByParameter(
                WeekPlannerSlot.class,
                "SELECT DISTINCT s FROM WeekPlannerSlot s LEFT JOIN FETCH s.groups WHERE s.user.id = :userId ORDER BY s.dayOfWeek, s.hour",
                "userId",
                userId);
    }

    @Override
    public List<WeekPlannerSlot> findByUserIdAndDayOfWeek(String userId, int dayOfWeek) {
        return JpaTransactionHelper.executeReadOnly(em ->
                em.createQuery(
                        "SELECT DISTINCT s FROM WeekPlannerSlot s LEFT JOIN FETCH s.groups WHERE s.user.id = :userId AND s.dayOfWeek = :dayOfWeek ORDER BY s.hour",
                        WeekPlannerSlot.class)
                        .setParameter("userId", userId)
                        .setParameter("dayOfWeek", dayOfWeek)
                        .getResultList());
    }
}
