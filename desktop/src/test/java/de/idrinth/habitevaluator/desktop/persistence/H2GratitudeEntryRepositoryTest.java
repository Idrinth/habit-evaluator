package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.GratitudeEntry;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2GratitudeEntryRepositoryTest extends H2RepositoryTestBase {

    private H2GratitudeEntryRepository gratitudeRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        gratitudeRepository = new H2GratitudeEntryRepository();
        userRepository = new H2UserRepository();

        for (GratitudeEntry entry : gratitudeRepository.findAll()) {
            gratitudeRepository.deleteById(entry.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("gratitudeuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        GratitudeEntry entry = new GratitudeEntry("I'm grateful for my health");
        entry.setUser(testUser);
        GratitudeEntry saved = gratitudeRepository.save(entry);

        assertNotNull(saved);
        Optional<GratitudeEntry> found = gratitudeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("I'm grateful for my health", found.get().getDescription());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<GratitudeEntry> result = gratitudeRepository.findById("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        GratitudeEntry e1 = new GratitudeEntry("Entry 1");
        e1.setUser(testUser);
        gratitudeRepository.save(e1);

        GratitudeEntry e2 = new GratitudeEntry("Entry 2");
        e2.setUser(testUser);
        gratitudeRepository.save(e2);

        List<GratitudeEntry> all = gratitudeRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        GratitudeEntry entry = new GratitudeEntry("To delete");
        entry.setUser(testUser);
        gratitudeRepository.save(entry);

        gratitudeRepository.deleteById(entry.getId());
        assertFalse(gratitudeRepository.findById(entry.getId()).isPresent());
    }

    @Test
    void testFindByUserId() {
        GratitudeEntry e1 = new GratitudeEntry("User entry");
        e1.setUser(testUser);
        gratitudeRepository.save(e1);

        User otherUser = new User("otheruser", "password");
        userRepository.save(otherUser);
        GratitudeEntry e2 = new GratitudeEntry("Other entry");
        e2.setUser(otherUser);
        gratitudeRepository.save(e2);

        List<GratitudeEntry> found = gratitudeRepository.findByUserId(testUser.getId());
        assertEquals(1, found.size());
        assertEquals("User entry", found.get(0).getDescription());
    }

    @Test
    void testSaveWithEventDate() {
        GratitudeEntry entry = new GratitudeEntry("Dated gratitude", LocalDate.of(2026, 3, 15));
        entry.setUser(testUser);
        gratitudeRepository.save(entry);

        Optional<GratitudeEntry> found = gratitudeRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalDate.of(2026, 3, 15), found.get().getEventDate());
    }
}
