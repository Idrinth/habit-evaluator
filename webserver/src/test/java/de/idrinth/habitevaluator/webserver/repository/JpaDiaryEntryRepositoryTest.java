package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaDiaryEntryRepositoryTest {

    @Autowired
    private JpaDiaryEntryRepository diaryEntryRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("diaryuser", "password123", "diaryuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindByUserId() {
        DiaryEntry entry = new DiaryEntry("Good day at work", EventSignificance.NORMAL);
        entry.setUser(testUser);
        diaryEntryRepository.save(entry);

        List<DiaryEntry> entries = diaryEntryRepository.findByUserId(testUser.getId());

        assertEquals(1, entries.size());
        assertEquals("Good day at work", entries.get(0).getDescription());
    }

    @Test
    void testFindDistinctDescriptionsByUserId() {
        DiaryEntry entry1 = new DiaryEntry("Meeting", EventSignificance.NORMAL);
        entry1.setUser(testUser);
        diaryEntryRepository.save(entry1);

        DiaryEntry entry2 = new DiaryEntry("Meeting", EventSignificance.MAJOR);
        entry2.setUser(testUser);
        diaryEntryRepository.save(entry2);

        DiaryEntry entry3 = new DiaryEntry("Exercise", EventSignificance.MINOR);
        entry3.setUser(testUser);
        diaryEntryRepository.save(entry3);

        List<String> descriptions = diaryEntryRepository.findDistinctDescriptionsByUserId(testUser.getId());

        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains("Meeting"));
        assertTrue(descriptions.contains("Exercise"));
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        DiaryEntry entry = new DiaryEntry("Test", EventSignificance.MINOR);
        entry.setUser(testUser);
        diaryEntryRepository.save(entry);

        User otherUser = new User("otherdiaryuser", "password", "otherdiary@example.com");
        otherUser = userRepository.save(otherUser);

        List<DiaryEntry> entries = diaryEntryRepository.findByUserId(otherUser.getId());
        assertTrue(entries.isEmpty());
    }

    @Test
    void testDeleteById() {
        DiaryEntry entry = new DiaryEntry("To delete", EventSignificance.MINOR);
        entry.setUser(testUser);
        DiaryEntry saved = diaryEntryRepository.save(entry);

        diaryEntryRepository.deleteById(saved.getId());

        assertFalse(diaryEntryRepository.findById(saved.getId()).isPresent());
    }
}
