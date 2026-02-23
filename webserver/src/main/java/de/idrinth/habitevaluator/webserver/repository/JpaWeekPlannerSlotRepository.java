package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaWeekPlannerSlotRepository extends JpaRepository<WeekPlannerSlot, String> {

    @Query("SELECT s FROM WeekPlannerSlot s WHERE s.user.id = :userId ORDER BY s.dayOfWeek, s.hour")
    List<WeekPlannerSlot> findByUserId(@Param("userId") String userId);

    @Query("SELECT s FROM WeekPlannerSlot s WHERE s.user.id = :userId AND s.dayOfWeek = :dayOfWeek ORDER BY s.hour")
    List<WeekPlannerSlot> findByUserIdAndDayOfWeek(@Param("userId") String userId, @Param("dayOfWeek") int dayOfWeek);
}
