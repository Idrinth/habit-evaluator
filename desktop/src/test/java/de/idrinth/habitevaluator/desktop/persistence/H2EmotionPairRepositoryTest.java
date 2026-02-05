package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2EmotionPairRepositoryTest extends H2RepositoryTestBase {

    private H2EmotionPairRepository pairRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        pairRepository = new H2EmotionPairRepository();
        userRepository = new H2UserRepository();

        for (EmotionPair pair : pairRepository.findAll()) {
            pairRepository.deleteById(pair.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("emotionuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(testUser);
        EmotionPair saved = pairRepository.save(pair);

        assertNotNull(saved);
        Optional<EmotionPair> found = pairRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Sad", found.get().getNegativeLabel());
        assertEquals("Happy", found.get().getPositiveLabel());
    }

    @Test
    void testSaveUpdatesExistingPair() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(testUser);
        pairRepository.save(pair);

        pair.setNegativeLabel("Listless");
        pair.setPositiveLabel("Active");
        pairRepository.save(pair);

        Optional<EmotionPair> found = pairRepository.findById(pair.getId());
        assertTrue(found.isPresent());
        assertEquals("Listless", found.get().getNegativeLabel());
        assertEquals("Active", found.get().getPositiveLabel());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<EmotionPair> found = pairRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        EmotionPair pair1 = new EmotionPair("Sad", "Happy");
        pair1.setUser(testUser);
        EmotionPair pair2 = new EmotionPair("Anxious", "Calm");
        pair2.setUser(testUser);
        pairRepository.save(pair1);
        pairRepository.save(pair2);

        List<EmotionPair> all = pairRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        EmotionPair pair = new EmotionPair("Sad", "Happy");
        pair.setUser(testUser);
        pairRepository.save(pair);

        Optional<EmotionPair> found = pairRepository.findById(pair.getId());
        assertTrue(found.isPresent());

        pairRepository.deleteById(pair.getId());

        found = pairRepository.findById(pair.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> pairRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        EmotionPair pair1 = new EmotionPair("Sad", "Happy");
        pair1.setUser(testUser);
        pairRepository.save(pair1);

        User otherUser = new User("otheremotionuser", "password123");
        userRepository.save(otherUser);
        EmotionPair pair2 = new EmotionPair("Anxious", "Calm");
        pair2.setUser(otherUser);
        pairRepository.save(pair2);

        List<EmotionPair> userPairs = pairRepository.findByUserId(testUser.getId());
        assertEquals(1, userPairs.size());
        assertEquals("Sad", userPairs.get(0).getNegativeLabel());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<EmotionPair> pairs = pairRepository.findByUserId("nonexistent-user-id");
        assertTrue(pairs.isEmpty());
    }
}
