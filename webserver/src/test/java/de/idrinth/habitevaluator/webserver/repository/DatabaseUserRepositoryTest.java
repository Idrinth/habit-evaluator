package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseUserRepositoryTest {

    private JpaUserRepository jpaRepository;
    private DatabaseUserRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaUserRepository.class);
        repository = new DatabaseUserRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        User user = new User("testuser", "password");
        when(jpaRepository.save(user)).thenReturn(user);

        User result = repository.save(user);

        assertSame(user, result);
        verify(jpaRepository).save(user);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        User user = new User("testuser", "password");
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(user));

        Optional<User> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(user, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<User> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsernameDelegatesToJpa() {
        User user = new User("testuser", "password");
        when(jpaRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = repository.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(jpaRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsernameReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = repository.findByUsername("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmailDelegatesToJpa() {
        User user = new User("testuser", "password");
        when(jpaRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = repository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        verify(jpaRepository).findByEmail("test@example.com");
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<User> users = List.of(new User("u1", "p1"), new User("u2", "p2"));
        when(jpaRepository.findAll()).thenReturn(users);

        List<User> result = repository.findAll();

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

        assertTrue(repository.existsById("id-1"));
        verify(jpaRepository).existsById("id-1");
    }

    @Test
    void testExistsByUsernameDelegatesToJpa() {
        when(jpaRepository.existsByUsername("testuser")).thenReturn(true);
        when(jpaRepository.existsByUsername("unknown")).thenReturn(false);

        assertTrue(repository.existsByUsername("testuser"));
        assertFalse(repository.existsByUsername("unknown"));
        verify(jpaRepository).existsByUsername("testuser");
        verify(jpaRepository).existsByUsername("unknown");
    }
}
