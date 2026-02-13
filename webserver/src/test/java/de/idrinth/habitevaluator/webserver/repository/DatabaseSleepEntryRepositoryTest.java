package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseSleepEntryRepositoryTest {

    private JpaSleepEntryRepository jpaRepository;
    private DatabaseSleepEntryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaSleepEntryRepository.class);
        repository = new DatabaseSleepEntryRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        SleepEntry entry = new SleepEntry();
        when(jpaRepository.save(entry)).thenReturn(entry);

        SleepEntry result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        SleepEntry entry = new SleepEntry();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<SleepEntry> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<SleepEntry> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<SleepEntry> entries = List.of(new SleepEntry(), new SleepEntry());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<SleepEntry> result = repository.findAll();

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
        List<SleepEntry> entries = List.of(new SleepEntry());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<SleepEntry> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
