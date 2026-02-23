package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PlannerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPlannerGroupRepository extends JpaRepository<PlannerGroup, String> {

    @Query("SELECT g FROM PlannerGroup g WHERE g.user.id = :userId ORDER BY g.name")
    List<PlannerGroup> findByUserId(@Param("userId") String userId);
}
