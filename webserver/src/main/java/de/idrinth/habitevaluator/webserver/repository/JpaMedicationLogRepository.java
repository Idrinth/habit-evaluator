package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MedicationLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMedicationLogRepository extends JpaRepository<MedicationLog, String> {

    @Query("SELECT m FROM MedicationLog m WHERE m.user.id = :userId")
    List<MedicationLog> findByUserId(@Param("userId") String userId);

    @Query("SELECT m FROM MedicationLog m WHERE m.user.id = :userId ORDER BY m.takenAt DESC, m.createdAt DESC")
    List<MedicationLog> findByUserIdPaged(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM MedicationLog m WHERE m.user.id = :userId")
    int countByUserId(@Param("userId") String userId);
}
