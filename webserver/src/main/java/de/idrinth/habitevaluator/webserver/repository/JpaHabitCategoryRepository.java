package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaHabitCategoryRepository extends JpaRepository<HabitCategory, String> {
    @Query("SELECT c FROM HabitCategory c WHERE c.user.id = :userId ORDER BY LOWER(c.name)")
    List<HabitCategory> findByUserId(@Param("userId") String userId);
}
