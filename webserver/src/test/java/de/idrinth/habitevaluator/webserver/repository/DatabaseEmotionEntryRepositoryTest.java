package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseEmotionEntryRepositoryTest {

    private JpaEmotionEntryRepository jpaRepository;
    private DatabaseEmotionEntryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaEmotionEntryRepository.class);
        repository = new DatabaseEmotionEntryRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        EmotionEntry entry = new EmotionEntry();
        when(jpaRepository.save(entry)).thenReturn(entry);

        EmotionEntry result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        EmotionEntry entry = new EmotionEntry();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<EmotionEntry> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<EmotionEntry> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<EmotionEntry> entries = List.of(new EmotionEntry(), new EmotionEntry());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<EmotionEntry> result = repository.findAll();

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
        List<EmotionEntry> entries = List.of(new EmotionEntry());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<EmotionEntry> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
