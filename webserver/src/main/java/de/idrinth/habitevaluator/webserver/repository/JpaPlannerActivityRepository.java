package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPlannerActivityRepository extends JpaRepository<PlannerActivity, String> {

    @Query("SELECT a FROM PlannerActivity a WHERE a.user.id = :userId ORDER BY a.name")
    List<PlannerActivity> findByUserId(@Param("userId") String userId);

    @Query("SELECT DISTINCT a FROM PlannerActivity a JOIN a.groups g WHERE g.id = :groupId ORDER BY a.name")
    List<PlannerActivity> findByGroupId(@Param("groupId") String groupId);
}
