package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSportLogRepository extends JpaRepository<SportLog, String> {

    @Query("SELECT s FROM SportLog s WHERE s.user.id = :userId")
    List<SportLog> findByUserId(@Param("userId") String userId);
}
