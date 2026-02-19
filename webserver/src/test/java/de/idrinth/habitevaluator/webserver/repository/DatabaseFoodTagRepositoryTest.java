package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.FoodTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseFoodTagRepositoryTest {

    private JpaFoodTagRepository jpaRepository;
    private DatabaseFoodTagRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaFoodTagRepository.class);
        repository = new DatabaseFoodTagRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        FoodTag tag = new FoodTag();
        when(jpaRepository.save(tag)).thenReturn(tag);

        FoodTag result = repository.save(tag);

        assertSame(tag, result);
        verify(jpaRepository).save(tag);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        FoodTag tag = new FoodTag();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(tag));

        Optional<FoodTag> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(tag, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<FoodTag> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<FoodTag> tags = List.of(new FoodTag());
        when(jpaRepository.findByUserId("user-1")).thenReturn(tags);

        List<FoodTag> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByNameLowerAndUserIdDelegatesToJpa() {
        FoodTag tag = new FoodTag();
        when(jpaRepository.findByNameLowerAndUserId("dairy", "user-1")).thenReturn(Optional.of(tag));

        Optional<FoodTag> result = repository.findByNameLowerAndUserId("dairy", "user-1");

        assertTrue(result.isPresent());
        assertSame(tag, result.get());
        verify(jpaRepository).findByNameLowerAndUserId("dairy", "user-1");
    }

    @Test
    void testFindByNameLowerAndUserIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByNameLowerAndUserId("unknown", "user-1")).thenReturn(Optional.empty());

        Optional<FoodTag> result = repository.findByNameLowerAndUserId("unknown", "user-1");

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteByIdDelegatesToJpa() {
        repository.deleteById("id-1");

        verify(jpaRepository).deleteById("id-1");
    }

    @Test
    void testDeleteEmptyTagsDelegatesToJpa() {
        repository.deleteEmptyTags("user-1");

        verify(jpaRepository).removeEmptyTagLinks("user-1");
        verify(jpaRepository).removeEmptyTags("user-1");
    }
}
