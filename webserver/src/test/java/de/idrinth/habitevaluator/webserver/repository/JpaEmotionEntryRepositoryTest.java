package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaEmotionEntryRepositoryTest {

    @Autowired
    private JpaEmotionEntryRepository emotionEntryRepository;

    @Autowired
    private JpaEmotionPairRepository emotionPairRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;
    private EmotionPair testPair;

    @BeforeEach
    void setUp() {
        testUser = new User("entryuser", "password123", "entryuser@example.com");
        testUser = userRepository.save(testUser);

        testPair = new EmotionPair("sad", "happy");
        testPair.setUser(testUser);
        testPair = emotionPairRepository.save(testPair);
    }

    @Test
    void testSaveAndFindById() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 30);
        EmotionEntry entry = new EmotionEntry(testPair, 5, now, "Feeling good");
        entry.setUser(testUser);
        EmotionEntry saved = emotionEntryRepository.save(entry);

        Optional<EmotionEntry> found = emotionEntryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(5, found.get().getStrength());
        assertEquals("Feeling good", found.get().getNotes());
        assertEquals(now, found.get().getRecordedAt());
    }

    @Test
    void testFindByUserId() {
        EmotionEntry entry1 = new EmotionEntry(testPair, 3, LocalDateTime.of(2025, 1, 15, 10, 0), null);
        entry1.setUser(testUser);
        emotionEntryRepository.save(entry1);

        EmotionEntry entry2 = new EmotionEntry(testPair, -2, LocalDateTime.of(2025, 1, 15, 14, 0), null);
        entry2.setUser(testUser);
        emotionEntryRepository.save(entry2);

        List<EmotionEntry> entries = emotionEntryRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
    }

    @Test
    void testFindByUserIdOrderedByRecordedAtDesc() {
        EmotionEntry older = new EmotionEntry(testPair, 3, LocalDateTime.of(2025, 1, 10, 10, 0), null);
        older.setUser(testUser);
        emotionEntryRepository.save(older);

        EmotionEntry newer = new EmotionEntry(testPair, -2, LocalDateTime.of(2025, 1, 20, 14, 0), null);
        newer.setUser(testUser);
        emotionEntryRepository.save(newer);

        List<EmotionEntry> entries = emotionEntryRepository.findByUserId(testUser.getId());

        assertEquals(2, entries.size());
        assertEquals(LocalDateTime.of(2025, 1, 20, 14, 0), entries.get(0).getRecordedAt());
        assertEquals(LocalDateTime.of(2025, 1, 10, 10, 0), entries.get(1).getRecordedAt());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        EmotionEntry entry = new EmotionEntry(testPair, 5, LocalDateTime.now(), null);
        entry.setUser(testUser);
        emotionEntryRepository.save(entry);

        User otherUser = new User("otherentryuser", "password", "otherentry@example.com");
        otherUser = userRepository.save(otherUser);

        List<EmotionEntry> entries = emotionEntryRepository.findByUserId(otherUser.getId());
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDeleteById() {
        EmotionEntry entry = new EmotionEntry(testPair, 5, LocalDateTime.now(), null);
        entry.setUser(testUser);
        EmotionEntry saved = emotionEntryRepository.save(entry);

        emotionEntryRepository.deleteById(saved.getId());

        assertFalse(emotionEntryRepository.findById(saved.getId()).isPresent());
    }
}
