package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaReminderSettingsRepository extends JpaRepository<ReminderSettings, String> {

    @Query("SELECT rs FROM ReminderSettings rs WHERE rs.user.id = :userId")
    Optional<ReminderSettings> findByUserId(@Param("userId") String userId);

    void deleteByUserId(String userId);
}
