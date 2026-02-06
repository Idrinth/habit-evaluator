package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;

import java.util.Optional;

/**
 * Repository interface for user reminder settings.
 */
public interface ReminderSettingsRepository {

    ReminderSettings save(ReminderSettings settings);

    Optional<ReminderSettings> findByUserId(String userId);

    void deleteByUserId(String userId);
}
