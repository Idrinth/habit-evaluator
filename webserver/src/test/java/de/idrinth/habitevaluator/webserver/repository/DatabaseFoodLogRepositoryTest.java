package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseFoodLogRepositoryTest {

    private JpaFoodLogRepository jpaRepository;
    private DatabaseFoodLogRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaFoodLogRepository.class);
        repository = new DatabaseFoodLogRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        FoodLog entry = new FoodLog();
        when(jpaRepository.save(entry)).thenReturn(entry);

        FoodLog result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        FoodLog entry = new FoodLog();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<FoodLog> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<FoodLog> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<FoodLog> entries = List.of(new FoodLog(), new FoodLog());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<FoodLog> result = repository.findAll();

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
        List<FoodLog> entries = List.of(new FoodLog());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<FoodLog> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
