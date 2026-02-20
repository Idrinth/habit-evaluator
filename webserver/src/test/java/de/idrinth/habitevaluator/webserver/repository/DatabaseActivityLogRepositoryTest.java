package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ActivityLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseActivityLogRepositoryTest {

    private JpaActivityLogRepository jpaRepository;
    private DatabaseActivityLogRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaActivityLogRepository.class);
        repository = new DatabaseActivityLogRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        ActivityLog entry = new ActivityLog();
        when(jpaRepository.save(entry)).thenReturn(entry);

        ActivityLog result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        ActivityLog entry = new ActivityLog();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<ActivityLog> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<ActivityLog> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<ActivityLog> entries = List.of(new ActivityLog(), new ActivityLog());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<ActivityLog> result = repository.findAll();

        assertEquals(2, result.size());
        verify(jpaRepository).findAll();
    }

    @Test
    void testDeleteByIdDelegatesToJpa() {
        repository.deleteById("id-1");

        verify(jpaRepository).deleteById("id-1");
    }

    @Test
    void testExistsByIdDelegatesToJpa() {
        when(jpaRepository.existsById("id-1")).thenReturn(true);
        when(jpaRepository.existsById("id-2")).thenReturn(false);

        assertTrue(repository.existsById("id-1"));
        assertFalse(repository.existsById("id-2"));
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<ActivityLog> entries = List.of(new ActivityLog());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<ActivityLog> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindDistinctLocationsByUserIdDelegatesToJpa() {
        List<String> locations = List.of("Cafe", "Office");
        when(jpaRepository.findDistinctLocationsByUserId("user-1")).thenReturn(locations);

        List<String> result = repository.findDistinctLocationsByUserId("user-1");

        assertEquals(2, result.size());
        verify(jpaRepository).findDistinctLocationsByUserId("user-1");
    }

    @Test
    void testFindDistinctActivitiesByUserIdDelegatesToJpa() {
        List<String> activities = List.of("Lunch", "Team meeting");
        when(jpaRepository.findDistinctActivitiesByUserId("user-1")).thenReturn(activities);

        List<String> result = repository.findDistinctActivitiesByUserId("user-1");

        assertEquals(2, result.size());
        verify(jpaRepository).findDistinctActivitiesByUserId("user-1");
    }
}
