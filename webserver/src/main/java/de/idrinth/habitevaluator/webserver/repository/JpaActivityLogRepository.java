package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaActivityLogRepository extends JpaRepository<ActivityLog, String> {

    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.date DESC, a.startTime DESC")
    List<ActivityLog> findByUserId(@Param("userId") String userId);
}
