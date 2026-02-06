package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import de.idrinth.habitevaluator.shared.repository.ReminderSettingsRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DatabaseReminderSettingsRepository implements ReminderSettingsRepository {

    private final JpaReminderSettingsRepository jpaRepository;

    public DatabaseReminderSettingsRepository(JpaReminderSettingsRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReminderSettings save(ReminderSettings settings) {
        return jpaRepository.save(settings);
    }

    @Override
    public Optional<ReminderSettings> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
