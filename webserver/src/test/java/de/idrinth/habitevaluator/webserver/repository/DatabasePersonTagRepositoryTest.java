package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PersonTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabasePersonTagRepositoryTest {

    private JpaPersonTagRepository jpaRepository;
    private DatabasePersonTagRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaPersonTagRepository.class);
        repository = new DatabasePersonTagRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        PersonTag tag = new PersonTag();
        when(jpaRepository.save(tag)).thenReturn(tag);

        PersonTag result = repository.save(tag);

        assertSame(tag, result);
        verify(jpaRepository).save(tag);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        PersonTag tag = new PersonTag();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(tag));

        Optional<PersonTag> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(tag, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<PersonTag> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<PersonTag> tags = List.of(new PersonTag());
        when(jpaRepository.findByUserId("user-1")).thenReturn(tags);

        List<PersonTag> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByNameLowerAndUserIdDelegatesToJpa() {
        PersonTag tag = new PersonTag();
        when(jpaRepository.findByNameLowerAndUserId("alice", "user-1")).thenReturn(Optional.of(tag));

        Optional<PersonTag> result = repository.findByNameLowerAndUserId("alice", "user-1");

        assertTrue(result.isPresent());
        assertSame(tag, result.get());
        verify(jpaRepository).findByNameLowerAndUserId("alice", "user-1");
    }

    @Test
    void testFindByNameLowerAndUserIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByNameLowerAndUserId("unknown", "user-1")).thenReturn(Optional.empty());

        Optional<PersonTag> result = repository.findByNameLowerAndUserId("unknown", "user-1");

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
