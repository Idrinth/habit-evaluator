package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.Habit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseHabitRepositoryTest {

    private JpaHabitRepository jpaRepository;
    private DatabaseHabitRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaHabitRepository.class);
        repository = new DatabaseHabitRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        Habit habit = new Habit();
        when(jpaRepository.save(habit)).thenReturn(habit);

        Habit result = repository.save(habit);

        assertSame(habit, result);
        verify(jpaRepository).save(habit);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        Habit habit = new Habit();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(habit));

        Optional<Habit> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(habit, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<Habit> result = repository.findById("missing");

        assertFalse(result.isPresent());
        verify(jpaRepository).findById("missing");
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<Habit> habits = List.of(new Habit(), new Habit());
        when(jpaRepository.findAll()).thenReturn(habits);

        List<Habit> result = repository.findAll();

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
        verify(jpaRepository).existsById("id-1");
        verify(jpaRepository).existsById("id-2");
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<Habit> habits = List.of(new Habit());
        when(jpaRepository.findByUserId("user-1")).thenReturn(habits);

        List<Habit> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
