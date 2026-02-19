package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaEmotionPairRepositoryTest {

    @Autowired
    private JpaEmotionPairRepository emotionPairRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("emotionuser", "password123", "emotionuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        pair.setUser(testUser);
        EmotionPair saved = emotionPairRepository.save(pair);

        Optional<EmotionPair> found = emotionPairRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("sad", found.get().getNegativeLabel());
        assertEquals("happy", found.get().getPositiveLabel());
    }

    @Test
    void testFindByUserId() {
        EmotionPair pair1 = new EmotionPair("sad", "happy");
        pair1.setUser(testUser);
        emotionPairRepository.save(pair1);

        EmotionPair pair2 = new EmotionPair("anxious", "calm");
        pair2.setUser(testUser);
        emotionPairRepository.save(pair2);

        List<EmotionPair> pairs = emotionPairRepository.findByUserId(testUser.getId());

        assertEquals(2, pairs.size());
    }

    @Test
    void testFindByUserIdSortedByNegativeLabel() {
        EmotionPair pairZ = new EmotionPair("tired", "energetic");
        pairZ.setUser(testUser);
        emotionPairRepository.save(pairZ);

        EmotionPair pairA = new EmotionPair("anxious", "calm");
        pairA.setUser(testUser);
        emotionPairRepository.save(pairA);

        List<EmotionPair> pairs = emotionPairRepository.findByUserId(testUser.getId());

        assertEquals(2, pairs.size());
        assertEquals("anxious", pairs.get(0).getNegativeLabel());
        assertEquals("tired", pairs.get(1).getNegativeLabel());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        pair.setUser(testUser);
        emotionPairRepository.save(pair);

        User otherUser = new User("otheremotionuser", "password", "otheremotion@example.com");
        otherUser = userRepository.save(otherUser);

        List<EmotionPair> pairs = emotionPairRepository.findByUserId(otherUser.getId());
        assertTrue(pairs.isEmpty());
    }

    @Test
    void testDeleteById() {
        EmotionPair pair = new EmotionPair("sad", "happy");
        pair.setUser(testUser);
        EmotionPair saved = emotionPairRepository.save(pair);

        emotionPairRepository.deleteById(saved.getId());

        assertFalse(emotionPairRepository.findById(saved.getId()).isPresent());
    }
}
