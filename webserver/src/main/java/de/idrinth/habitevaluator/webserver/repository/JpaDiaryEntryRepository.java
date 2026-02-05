package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaDiaryEntryRepository extends JpaRepository<DiaryEntry, String> {

    @Query("SELECT d FROM DiaryEntry d WHERE d.user.id = :userId")
    List<DiaryEntry> findByUserId(@Param("userId") String userId);

    @Query("SELECT DISTINCT d.description FROM DiaryEntry d WHERE d.user.id = :userId ORDER BY d.description")
    List<String> findDistinctDescriptionsByUserId(@Param("userId") String userId);
}
