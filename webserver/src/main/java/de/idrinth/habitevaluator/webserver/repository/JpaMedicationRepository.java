package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMedicationRepository extends JpaRepository<Medication, String> {

    @Query("SELECT m FROM Medication m WHERE m.user.id = :userId ORDER BY LOWER(m.name)")
    List<Medication> findByUserId(@Param("userId") String userId);
}
