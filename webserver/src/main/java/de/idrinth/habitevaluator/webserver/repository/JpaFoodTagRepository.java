package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaFoodTagRepository extends JpaRepository<FoodTag, String> {

    @Query("SELECT t FROM FoodTag t WHERE t.user.id = :userId ORDER BY LOWER(t.name)")
    List<FoodTag> findByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM FoodTag t WHERE t.nameLower = :nameLower AND t.user.id = :userId")
    Optional<FoodTag> findByNameLowerAndUserId(@Param("nameLower") String nameLower,
                                               @Param("userId") String userId);
}
