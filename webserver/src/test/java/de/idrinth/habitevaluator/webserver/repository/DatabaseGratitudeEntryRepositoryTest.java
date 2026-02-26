package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseGratitudeEntryRepositoryTest {

    private JpaGratitudeEntryRepository jpaRepository;
    private DatabaseGratitudeEntryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaGratitudeEntryRepository.class);
        repository = new DatabaseGratitudeEntryRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        GratitudeEntry entry = new GratitudeEntry();
        when(jpaRepository.save(entry)).thenReturn(entry);

        GratitudeEntry result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        GratitudeEntry entry = new GratitudeEntry();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<GratitudeEntry> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<GratitudeEntry> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<GratitudeEntry> entries = List.of(new GratitudeEntry(), new GratitudeEntry());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<GratitudeEntry> result = repository.findAll();

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
        List<GratitudeEntry> entries = List.of(new GratitudeEntry());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<GratitudeEntry> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
