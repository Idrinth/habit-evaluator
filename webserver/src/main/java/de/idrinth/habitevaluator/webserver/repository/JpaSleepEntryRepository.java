package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSleepEntryRepository extends JpaRepository<SleepEntry, String> {

    @Query("SELECT s FROM SleepEntry s WHERE s.user.id = :userId")
    List<SleepEntry> findByUserId(@Param("userId") String userId);
}
