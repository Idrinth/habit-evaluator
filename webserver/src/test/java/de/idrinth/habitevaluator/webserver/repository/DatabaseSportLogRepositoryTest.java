package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SportLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseSportLogRepositoryTest {

    private JpaSportLogRepository jpaRepository;
    private DatabaseSportLogRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaSportLogRepository.class);
        repository = new DatabaseSportLogRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        SportLog entry = new SportLog();
        when(jpaRepository.save(entry)).thenReturn(entry);

        SportLog result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        SportLog entry = new SportLog();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<SportLog> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<SportLog> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<SportLog> entries = List.of(new SportLog(), new SportLog());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<SportLog> result = repository.findAll();

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
        List<SportLog> entries = List.of(new SportLog());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<SportLog> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindDistinctNamesByUserIdDelegatesToJpa() {
        List<String> names = List.of("Running", "Swimming");
        when(jpaRepository.findDistinctNamesByUserId("user-1")).thenReturn(names);

        List<String> result = repository.findDistinctNamesByUserId("user-1");

        assertEquals(2, result.size());
        assertEquals("Running", result.get(0));
        assertEquals("Swimming", result.get(1));
        verify(jpaRepository).findDistinctNamesByUserId("user-1");
    }

    @Test
    void testFindDistinctMeasurementUnitsByUserIdDelegatesToJpa() {
        List<String> units = List.of("km", "m");
        when(jpaRepository.findDistinctMeasurementUnitsByUserId("user-1")).thenReturn(units);

        List<String> result = repository.findDistinctMeasurementUnitsByUserId("user-1");

        assertEquals(2, result.size());
        assertEquals("km", result.get(0));
        assertEquals("m", result.get(1));
        verify(jpaRepository).findDistinctMeasurementUnitsByUserId("user-1");
    }
}
