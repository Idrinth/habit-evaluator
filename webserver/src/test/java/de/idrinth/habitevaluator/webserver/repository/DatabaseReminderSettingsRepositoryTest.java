package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ReminderSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseReminderSettingsRepositoryTest {

    private JpaReminderSettingsRepository jpaRepository;
    private DatabaseReminderSettingsRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaReminderSettingsRepository.class);
        repository = new DatabaseReminderSettingsRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        ReminderSettings settings = new ReminderSettings();
        when(jpaRepository.save(settings)).thenReturn(settings);

        ReminderSettings result = repository.save(settings);

        assertSame(settings, result);
        verify(jpaRepository).save(settings);
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        ReminderSettings settings = new ReminderSettings();
        when(jpaRepository.findByUserId("user-1")).thenReturn(Optional.of(settings));

        Optional<ReminderSettings> result = repository.findByUserId("user-1");

        assertTrue(result.isPresent());
        assertSame(settings, result.get());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByUserIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        Optional<ReminderSettings> result = repository.findByUserId("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteByUserIdDelegatesToJpa() {
        repository.deleteByUserId("user-1");

        verify(jpaRepository).deleteByUserId("user-1");
    }
}
