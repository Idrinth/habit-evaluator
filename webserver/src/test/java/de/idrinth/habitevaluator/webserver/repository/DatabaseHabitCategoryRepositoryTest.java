package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseHabitCategoryRepositoryTest {

    private JpaHabitCategoryRepository jpaRepository;
    private DatabaseHabitCategoryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaHabitCategoryRepository.class);
        repository = new DatabaseHabitCategoryRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        HabitCategory category = new HabitCategory("Health", "desc", "#00FF00");
        when(jpaRepository.save(category)).thenReturn(category);

        HabitCategory result = repository.save(category);

        assertSame(category, result);
        verify(jpaRepository).save(category);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        HabitCategory category = new HabitCategory("Health", "desc", "#00FF00");
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(category));

        Optional<HabitCategory> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(category, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<HabitCategory> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<HabitCategory> categories = List.of(
                new HabitCategory("Health", "desc", "#00FF00"),
                new HabitCategory("Work", "desc", "#0000FF")
        );
        when(jpaRepository.findAll()).thenReturn(categories);

        List<HabitCategory> result = repository.findAll();

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
        List<HabitCategory> categories = List.of(new HabitCategory("Health", "desc", "#00FF00"));
        when(jpaRepository.findByUserId("user-1")).thenReturn(categories);

        List<HabitCategory> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
