package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MedicationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseMedicationLogRepositoryTest {

    private JpaMedicationLogRepository jpaRepository;
    private DatabaseMedicationLogRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaMedicationLogRepository.class);
        repository = new DatabaseMedicationLogRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        MedicationLog entry = new MedicationLog();
        when(jpaRepository.save(entry)).thenReturn(entry);

        MedicationLog result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        MedicationLog entry = new MedicationLog();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<MedicationLog> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<MedicationLog> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<MedicationLog> entries = List.of(new MedicationLog(), new MedicationLog());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<MedicationLog> result = repository.findAll();

        assertEquals(2, result.size());
        verify(jpaRepository).findAll();
    }

    @Test
    void testDeleteByIdDelegatesToJpa() {
        repository.deleteById("id-1");

        verify(jpaRepository).deleteById("id-1");
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<MedicationLog> entries = List.of(new MedicationLog());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<MedicationLog> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByUserIdPagedDelegatesToJpa() {
        List<MedicationLog> entries = List.of(new MedicationLog());
        when(jpaRepository.findByUserIdPaged("user-1", PageRequest.of(0, 10))).thenReturn(entries);

        List<MedicationLog> result = repository.findByUserIdPaged("user-1", 10, 0);

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserIdPaged("user-1", PageRequest.of(0, 10));
    }

    @Test
    void testFindByUserIdPagedCalculatesPageCorrectly() {
        when(jpaRepository.findByUserIdPaged("user-1", PageRequest.of(2, 5))).thenReturn(List.of());

        repository.findByUserIdPaged("user-1", 5, 10);

        verify(jpaRepository).findByUserIdPaged("user-1", PageRequest.of(2, 5));
    }

    @Test
    void testFindByUserIdPagedWithZeroLimitThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                repository.findByUserIdPaged("user-1", 0, 0));
    }

    @Test
    void testCountByUserIdDelegatesToJpa() {
        when(jpaRepository.countByUserId("user-1")).thenReturn(5);

        int result = repository.countByUserId("user-1");

        assertEquals(5, result);
        verify(jpaRepository).countByUserId("user-1");
    }

    @Test
    void testCountByUserIdReturnsZeroForNoEntries() {
        when(jpaRepository.countByUserId("user-empty")).thenReturn(0);

        int result = repository.countByUserId("user-empty");

        assertEquals(0, result);
    }
}
