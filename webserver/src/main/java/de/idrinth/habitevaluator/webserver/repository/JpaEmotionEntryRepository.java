package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaEmotionEntryRepository extends JpaRepository<EmotionEntry, String> {

    @Query("SELECT e FROM EmotionEntry e WHERE e.user.id = :userId ORDER BY e.recordedAt DESC")
    List<EmotionEntry> findByUserId(@Param("userId") String userId);
}
