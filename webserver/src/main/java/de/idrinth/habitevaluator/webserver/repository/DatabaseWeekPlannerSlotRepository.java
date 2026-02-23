package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseWeekPlannerSlotRepository implements WeekPlannerSlotRepository {

    private final JpaWeekPlannerSlotRepository jpaRepository;

    public DatabaseWeekPlannerSlotRepository(JpaWeekPlannerSlotRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WeekPlannerSlot save(WeekPlannerSlot slot) {
        return jpaRepository.save(slot);
    }

    @Override
    public Optional<WeekPlannerSlot> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<WeekPlannerSlot> findAll() {
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
    public List<WeekPlannerSlot> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<WeekPlannerSlot> findByUserIdAndDayOfWeek(String userId, int dayOfWeek) {
        return jpaRepository.findByUserIdAndDayOfWeek(userId, dayOfWeek);
    }
}
