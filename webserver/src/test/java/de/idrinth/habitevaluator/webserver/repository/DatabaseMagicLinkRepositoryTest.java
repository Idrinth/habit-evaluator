package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MagicLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseMagicLinkRepositoryTest {

    private JpaMagicLinkRepository jpaRepository;
    private DatabaseMagicLinkRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaMagicLinkRepository.class);
        repository = new DatabaseMagicLinkRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        MagicLink link = new MagicLink();
        when(jpaRepository.save(link)).thenReturn(link);

        MagicLink result = repository.save(link);

        assertSame(link, result);
        verify(jpaRepository).save(link);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        MagicLink link = new MagicLink();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(link));

        Optional<MagicLink> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(link, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<MagicLink> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByTokenDelegatesToJpa() {
        MagicLink link = new MagicLink();
        when(jpaRepository.findByToken("abc123")).thenReturn(Optional.of(link));

        Optional<MagicLink> result = repository.findByToken("abc123");

        assertTrue(result.isPresent());
        assertSame(link, result.get());
        verify(jpaRepository).findByToken("abc123");
    }

    @Test
    void testFindByTokenReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByToken("unknown")).thenReturn(Optional.empty());

        Optional<MagicLink> result = repository.findByToken("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<MagicLink> links = List.of(new MagicLink(), new MagicLink());
        when(jpaRepository.findByUserId("user-1")).thenReturn(links);

        List<MagicLink> result = repository.findByUserId("user-1");

        assertEquals(2, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testDeleteByIdDelegatesToJpa() {
        repository.deleteById("id-1");

        verify(jpaRepository).deleteById("id-1");
    }
}
