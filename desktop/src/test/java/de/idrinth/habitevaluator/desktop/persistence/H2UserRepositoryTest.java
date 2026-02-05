package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2UserRepositoryTest extends H2RepositoryTestBase {

    private H2UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new H2UserRepository();
        // Clean up existing users
        for (User user : repository.findAll()) {
            repository.deleteById(user.getId());
        }
    }

    @Test
    void testSaveAndFindById() {
        User user = new User("testuser", "password123");
        User saved = repository.save(user);

        assertNotNull(saved);
        assertEquals(user.getId(), saved.getId());

        Optional<User> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testSaveUpdatesExistingUser() {
        User user = new User("testuser", "password123");
        repository.save(user);

        user.setUsername("updateduser");
        repository.save(user);

        Optional<User> found = repository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("updateduser", found.get().getUsername());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<User> found = repository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUsername() {
        User user = new User("uniqueuser", "password123");
        repository.save(user);

        Optional<User> found = repository.findByUsername("uniqueuser");
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
    }

    @Test
    void testFindByUsernameReturnsEmptyForNonExistent() {
        Optional<User> found = repository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail() {
        User user = new User("emailuser", "password123", "test@example.com");
        repository.save(user);

        Optional<User> found = repository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
    }

    @Test
    void testFindByEmailReturnsEmptyForNonExistent() {
        Optional<User> found = repository.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");
        repository.save(user1);
        repository.save(user2);

        List<User> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        User user = new User("deleteuser", "password123");
        repository.save(user);

        assertTrue(repository.existsById(user.getId()));
        repository.deleteById(user.getId());
        assertFalse(repository.existsById(user.getId()));
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> repository.deleteById("nonexistent-id"));
    }

    @Test
    void testExistsById() {
        User user = new User("existsuser", "password123");
        repository.save(user);

        assertTrue(repository.existsById(user.getId()));
        assertFalse(repository.existsById("nonexistent-id"));
    }

    @Test
    void testExistsByUsername() {
        User user = new User("checkuser", "password123");
        repository.save(user);

        assertTrue(repository.existsByUsername("checkuser"));
        assertFalse(repository.existsByUsername("nonexistent"));
    }
}
