package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ModuleVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseModuleVisibilityRepositoryTest {

    private JpaModuleVisibilityRepository jpaRepository;
    private DatabaseModuleVisibilityRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaModuleVisibilityRepository.class);
        repository = new DatabaseModuleVisibilityRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        ModuleVisibility settings = new ModuleVisibility();
        when(jpaRepository.save(settings)).thenReturn(settings);

        ModuleVisibility result = repository.save(settings);

        assertSame(settings, result);
        verify(jpaRepository).save(settings);
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        ModuleVisibility settings = new ModuleVisibility();
        when(jpaRepository.findByUserId("user-1")).thenReturn(Optional.of(settings));

        Optional<ModuleVisibility> result = repository.findByUserId("user-1");

        assertTrue(result.isPresent());
        assertSame(settings, result.get());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByUserIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        Optional<ModuleVisibility> result = repository.findByUserId("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteByUserIdDelegatesToJpa() {
        repository.deleteByUserId("user-1");

        verify(jpaRepository).deleteByUserId("user-1");
    }
}
