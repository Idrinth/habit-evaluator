package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaFoodLogRepository extends JpaRepository<FoodLog, String> {

    @Query("SELECT f FROM FoodLog f WHERE f.user.id = :userId ORDER BY f.dateTime DESC")
    List<FoodLog> findByUserId(@Param("userId") String userId);
}
