package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseEmotionPairRepositoryTest {

    private JpaEmotionPairRepository jpaRepository;
    private DatabaseEmotionPairRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaEmotionPairRepository.class);
        repository = new DatabaseEmotionPairRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        EmotionPair pair = new EmotionPair();
        when(jpaRepository.save(pair)).thenReturn(pair);

        EmotionPair result = repository.save(pair);

        assertSame(pair, result);
        verify(jpaRepository).save(pair);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        EmotionPair pair = new EmotionPair();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(pair));

        Optional<EmotionPair> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(pair, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<EmotionPair> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<EmotionPair> pairs = List.of(new EmotionPair(), new EmotionPair());
        when(jpaRepository.findAll()).thenReturn(pairs);

        List<EmotionPair> result = repository.findAll();

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
        List<EmotionPair> pairs = List.of(new EmotionPair());
        when(jpaRepository.findByUserId("user-1")).thenReturn(pairs);

        List<EmotionPair> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
