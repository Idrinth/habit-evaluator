package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PersonTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPersonTagRepository extends JpaRepository<PersonTag, String> {

    @Query("SELECT t FROM PersonTag t WHERE t.user.id = :userId ORDER BY LOWER(t.name)")
    List<PersonTag> findByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM PersonTag t WHERE t.nameLower = :nameLower AND t.user.id = :userId")
    Optional<PersonTag> findByNameLowerAndUserId(@Param("nameLower") String nameLower,
                                                  @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM activity_log_person_tags WHERE person_tag_id IN "
            + "(SELECT id FROM person_tags WHERE (name IS NULL OR TRIM(name) = '') AND user_id = :userId)",
            nativeQuery = true)
    void removeEmptyTagLinks(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM person_tags WHERE (name IS NULL OR TRIM(name) = '') AND user_id = :userId",
            nativeQuery = true)
    void removeEmptyTags(@Param("userId") String userId);
}
