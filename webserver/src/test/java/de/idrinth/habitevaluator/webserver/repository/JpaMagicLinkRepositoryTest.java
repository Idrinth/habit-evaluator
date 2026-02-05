package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.MagicLink;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaMagicLinkRepositoryTest {

    @Autowired
    private JpaMagicLinkRepository magicLinkRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("linkuser", "password123", "linkuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindByToken() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        magicLinkRepository.save(link);

        Optional<MagicLink> found = magicLinkRepository.findByToken(link.getToken());

        assertTrue(found.isPresent());
        assertEquals(link.getToken(), found.get().getToken());
    }

    @Test
    void testFindByTokenNotFound() {
        Optional<MagicLink> found = magicLinkRepository.findByToken("nonexistent-token");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUserId() {
        MagicLink link1 = new MagicLink();
        link1.setUser(testUser);
        magicLinkRepository.save(link1);

        MagicLink link2 = new MagicLink();
        link2.setUser(testUser);
        magicLinkRepository.save(link2);

        List<MagicLink> links = magicLinkRepository.findByUserId(testUser.getId());
        assertEquals(2, links.size());
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        magicLinkRepository.save(link);

        User otherUser = new User("otherlinkuser", "password", "otherlink@example.com");
        otherUser = userRepository.save(otherUser);

        List<MagicLink> links = magicLinkRepository.findByUserId(otherUser.getId());
        assertTrue(links.isEmpty());
    }

    @Test
    void testDeleteById() {
        MagicLink link = new MagicLink();
        link.setUser(testUser);
        MagicLink saved = magicLinkRepository.save(link);

        magicLinkRepository.deleteById(saved.getId());

        assertFalse(magicLinkRepository.findById(saved.getId()).isPresent());
    }
}
