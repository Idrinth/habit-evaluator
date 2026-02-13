package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Medication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseMedicationRepositoryTest {

    private JpaMedicationRepository jpaRepository;
    private DatabaseMedicationRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaMedicationRepository.class);
        repository = new DatabaseMedicationRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        Medication medication = new Medication();
        when(jpaRepository.save(medication)).thenReturn(medication);

        Medication result = repository.save(medication);

        assertSame(medication, result);
        verify(jpaRepository).save(medication);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        Medication medication = new Medication();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(medication));

        Optional<Medication> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(medication, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<Medication> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<Medication> medications = List.of(new Medication(), new Medication());
        when(jpaRepository.findAll()).thenReturn(medications);

        List<Medication> result = repository.findAll();

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
        List<Medication> medications = List.of(new Medication());
        when(jpaRepository.findByUserId("user-1")).thenReturn(medications);

        List<Medication> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
