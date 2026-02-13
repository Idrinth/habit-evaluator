package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseDiaryEntryRepositoryTest {

    private JpaDiaryEntryRepository jpaRepository;
    private DatabaseDiaryEntryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaDiaryEntryRepository.class);
        repository = new DatabaseDiaryEntryRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        DiaryEntry entry = new DiaryEntry();
        when(jpaRepository.save(entry)).thenReturn(entry);

        DiaryEntry result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        DiaryEntry entry = new DiaryEntry();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<DiaryEntry> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<DiaryEntry> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<DiaryEntry> entries = List.of(new DiaryEntry(), new DiaryEntry());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<DiaryEntry> result = repository.findAll();

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
        List<DiaryEntry> entries = List.of(new DiaryEntry());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<DiaryEntry> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindDistinctDescriptionsByUserIdDelegatesToJpa() {
        List<String> descriptions = List.of("Work", "Exercise", "Meeting");
        when(jpaRepository.findDistinctDescriptionsByUserId("user-1")).thenReturn(descriptions);

        List<String> result = repository.findDistinctDescriptionsByUserId("user-1");

        assertEquals(3, result.size());
        assertEquals("Work", result.get(0));
        verify(jpaRepository).findDistinctDescriptionsByUserId("user-1");
    }

    @Test
    void testFindEntriesNeedingMigrationDelegatesToJpa() {
        List<DiaryEntry> entries = List.of(new DiaryEntry());
        when(jpaRepository.findEntriesNeedingMigration("user-1")).thenReturn(entries);

        List<DiaryEntry> result = repository.findEntriesNeedingMigration("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findEntriesNeedingMigration("user-1");
    }
}
