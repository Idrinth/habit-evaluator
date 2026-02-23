package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for week planner slot persistence.
 */
public interface WeekPlannerSlotRepository {

    WeekPlannerSlot save(WeekPlannerSlot slot);

    Optional<WeekPlannerSlot> findById(String id);

    List<WeekPlannerSlot> findAll();

    void deleteById(String id);

    boolean existsById(String id);

    List<WeekPlannerSlot> findByUserId(String userId);

    List<WeekPlannerSlot> findByUserIdAndDayOfWeek(String userId, int dayOfWeek);
}
