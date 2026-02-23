package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SlotConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSlotConfirmationRepository extends JpaRepository<SlotConfirmation, String> {

    @Query("SELECT c FROM SlotConfirmation c WHERE c.user.id = :userId ORDER BY c.date DESC, c.createdAt DESC")
    List<SlotConfirmation> findByUserId(@Param("userId") String userId);
}
