package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaGratitudeEntryRepository extends JpaRepository<GratitudeEntry, String> {

    @Query("SELECT g FROM GratitudeEntry g WHERE g.user.id = :userId ORDER BY g.eventDate DESC, g.createdAt DESC")
    List<GratitudeEntry> findByUserId(@Param("userId") String userId);
}
