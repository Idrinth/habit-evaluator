package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MeetingEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseMeetingEntryRepositoryTest {

    private JpaMeetingEntryRepository jpaRepository;
    private DatabaseMeetingEntryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaMeetingEntryRepository.class);
        repository = new DatabaseMeetingEntryRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        MeetingEntry entry = new MeetingEntry();
        when(jpaRepository.save(entry)).thenReturn(entry);

        MeetingEntry result = repository.save(entry);

        assertSame(entry, result);
        verify(jpaRepository).save(entry);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        MeetingEntry entry = new MeetingEntry();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(entry));

        Optional<MeetingEntry> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(entry, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<MeetingEntry> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<MeetingEntry> entries = List.of(new MeetingEntry(), new MeetingEntry());
        when(jpaRepository.findAll()).thenReturn(entries);

        List<MeetingEntry> result = repository.findAll();

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
        List<MeetingEntry> entries = List.of(new MeetingEntry());
        when(jpaRepository.findByUserId("user-1")).thenReturn(entries);

        List<MeetingEntry> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
