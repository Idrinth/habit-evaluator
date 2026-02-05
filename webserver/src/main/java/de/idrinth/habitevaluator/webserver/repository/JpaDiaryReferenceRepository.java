package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaDiaryReferenceRepository extends JpaRepository<DiaryReference, String> {

    @Query("SELECT r FROM DiaryReference r WHERE r.user.id = :userId")
    List<DiaryReference> findByUserId(@Param("userId") String userId);

    @Query("SELECT r FROM DiaryReference r WHERE r.user.id = :userId AND r.descriptionLower = LOWER(:description)")
    Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(@Param("userId") String userId, @Param("description") String description);

    @Query("SELECT DISTINCT r.description FROM DiaryReference r WHERE r.user.id = :userId ORDER BY r.description")
    List<String> findDistinctDescriptionsByUserId(@Param("userId") String userId);
}
