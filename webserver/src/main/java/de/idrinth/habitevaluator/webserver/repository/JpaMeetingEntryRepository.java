package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMeetingEntryRepository extends JpaRepository<MeetingEntry, String> {

    @Query("SELECT m FROM MeetingEntry m WHERE m.user.id = :userId")
    List<MeetingEntry> findByUserId(@Param("userId") String userId);
}
