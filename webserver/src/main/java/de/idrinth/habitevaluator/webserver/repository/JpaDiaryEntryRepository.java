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

    @Query("SELECT DISTINCT COALESCE(d.diaryReference.description, d.legacyDescription) FROM DiaryEntry d WHERE d.user.id = :userId AND (d.diaryReference IS NOT NULL OR d.legacyDescription IS NOT NULL) ORDER BY COALESCE(d.diaryReference.description, d.legacyDescription)")
    List<String> findDistinctDescriptionsByUserId(@Param("userId") String userId);

    @Query("SELECT d FROM DiaryEntry d WHERE d.user.id = :userId AND d.diaryReference IS NULL AND d.legacyDescription IS NOT NULL")
    List<DiaryEntry> findEntriesNeedingMigration(@Param("userId") String userId);
}
