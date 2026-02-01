package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.HabitEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for HabitEntry entities.
 */
@Repository
public interface JpaHabitEntryRepository extends JpaRepository<HabitEntry, String> {

    /**
     * Find all entries for a specific habit.
     *
     * @param habitId the habit ID
     * @return list of entries for the habit
     */
    @Query("SELECT e FROM HabitEntry e WHERE e.habit.id = :habitId")
    List<HabitEntry> findByHabitId(@Param("habitId") String habitId);
}
