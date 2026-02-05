package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaUserRepositoryTest {

    @Autowired
    private JpaUserRepository userRepository;

    @Test
    void testSaveAndFindById() {
        User user = new User("testuser", "password123", "test@example.com");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsername() {
        User user = new User("uniqueuser", "password123", "unique@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("uniqueuser");

        assertTrue(found.isPresent());
        assertEquals("uniqueuser", found.get().getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail() {
        User user = new User("emailuser", "password123", "email@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("email@example.com");

        assertTrue(found.isPresent());
        assertEquals("emailuser", found.get().getUsername());
    }

    @Test
    void testExistsByUsername() {
        User user = new User("existsuser", "password123");
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("existsuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testDeleteUser() {
        User user = new User("deleteuser", "password123");
        User saved = userRepository.save(user);

        userRepository.deleteById(saved.getId());

        assertFalse(userRepository.findById(saved.getId()).isPresent());
    }
}
