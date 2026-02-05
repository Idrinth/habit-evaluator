package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2EmotionEntryRepositoryTest extends H2RepositoryTestBase {

    private H2EmotionEntryRepository entryRepository;
    private H2EmotionPairRepository pairRepository;
    private H2UserRepository userRepository;
    private User testUser;
    private EmotionPair testPair;

    @BeforeEach
    void setUp() {
        entryRepository = new H2EmotionEntryRepository();
        pairRepository = new H2EmotionPairRepository();
        userRepository = new H2UserRepository();

        for (EmotionEntry entry : entryRepository.findAll()) {
            entryRepository.deleteById(entry.getId());
        }
        for (EmotionPair pair : pairRepository.findAll()) {
            pairRepository.deleteById(pair.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("emotionentryuser", "password123");
        userRepository.save(testUser);

        testPair = new EmotionPair("Sad", "Happy");
        testPair.setUser(testUser);
        pairRepository.save(testPair);
    }

    @Test
    void testSaveAndFindById() {
        EmotionEntry entry = new EmotionEntry(testPair, 5, LocalDateTime.now(), "Feeling good");
        entry.setUser(testUser);
        EmotionEntry saved = entryRepository.save(entry);

        assertNotNull(saved);
        Optional<EmotionEntry> found = entryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getStrength());
        assertEquals("Feeling good", found.get().getNotes());
    }

    @Test
    void testSaveUpdatesExistingEntry() {
        EmotionEntry entry = new EmotionEntry(testPair, 3, LocalDateTime.now(), "OK");
        entry.setUser(testUser);
        entryRepository.save(entry);

        entry.setStrength(7);
        entry.setNotes("Better now");
        entryRepository.save(entry);

        Optional<EmotionEntry> found = entryRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(7, found.get().getStrength());
        assertEquals("Better now", found.get().getNotes());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<EmotionEntry> found = entryRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        EmotionEntry entry1 = new EmotionEntry(testPair, 5, LocalDateTime.now(), null);
        entry1.setUser(testUser);
        EmotionEntry entry2 = new EmotionEntry(testPair, -3, LocalDateTime.now().minusHours(1), null);
        entry2.setUser(testUser);
        entryRepository.save(entry1);
        entryRepository.save(entry2);

        List<EmotionEntry> all = entryRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        EmotionEntry entry = new EmotionEntry(testPair, 5, LocalDateTime.now(), null);
        entry.setUser(testUser);
        entryRepository.save(entry);

        Optional<EmotionEntry> found = entryRepository.findById(entry.getId());
        assertTrue(found.isPresent());

        entryRepository.deleteById(entry.getId());

        found = entryRepository.findById(entry.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> entryRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        EmotionEntry entry1 = new EmotionEntry(testPair, 5, LocalDateTime.now(), null);
        entry1.setUser(testUser);
        entryRepository.save(entry1);

        User otherUser = new User("otherentryuser", "password123");
        userRepository.save(otherUser);
        EmotionPair otherPair = new EmotionPair("Anxious", "Calm");
        otherPair.setUser(otherUser);
        pairRepository.save(otherPair);
        EmotionEntry entry2 = new EmotionEntry(otherPair, -2, LocalDateTime.now(), null);
        entry2.setUser(otherUser);
        entryRepository.save(entry2);

        List<EmotionEntry> userEntries = entryRepository.findByUserId(testUser.getId());
        assertEquals(1, userEntries.size());
        assertEquals(5, userEntries.get(0).getStrength());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<EmotionEntry> entries = entryRepository.findByUserId("nonexistent-user-id");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testStrengthClampedOnSave() {
        EmotionEntry entry = new EmotionEntry(testPair, 15, LocalDateTime.now(), null);
        entry.setUser(testUser);
        entryRepository.save(entry);

        Optional<EmotionEntry> found = entryRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getStrength());
    }
}
