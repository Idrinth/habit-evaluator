package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaEmotionPairRepository extends JpaRepository<EmotionPair, String> {

    @Query("SELECT e FROM EmotionPair e WHERE e.user.id = :userId")
    List<EmotionPair> findByUserId(@Param("userId") String userId);
}
