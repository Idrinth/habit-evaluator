package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2DiaryEntryRepositoryTest extends H2RepositoryTestBase {

    private H2DiaryEntryRepository diaryRepository;
    private H2UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        diaryRepository = new H2DiaryEntryRepository();
        userRepository = new H2UserRepository();

        for (DiaryEntry entry : diaryRepository.findAll()) {
            diaryRepository.deleteById(entry.getId());
        }
        for (User user : userRepository.findAll()) {
            userRepository.deleteById(user.getId());
        }

        testUser = new User("diaryuser", "password123");
        userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        DiaryEntry entry = new DiaryEntry("Great day", EventSignificance.MAJOR);
        entry.setUser(testUser);
        DiaryEntry saved = diaryRepository.save(entry);

        assertNotNull(saved);
        Optional<DiaryEntry> found = diaryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Great day", found.get().getDescription());
        assertEquals(EventSignificance.MAJOR, found.get().getSignificance());
    }

    @Test
    void testSaveUpdatesExistingEntry() {
        DiaryEntry entry = new DiaryEntry("Original", EventSignificance.MINOR);
        entry.setUser(testUser);
        diaryRepository.save(entry);

        entry.setDescription("Updated");
        diaryRepository.save(entry);

        Optional<DiaryEntry> found = diaryRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getDescription());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<DiaryEntry> found = diaryRepository.findById("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        DiaryEntry entry1 = new DiaryEntry("Entry 1", EventSignificance.MINOR);
        entry1.setUser(testUser);
        DiaryEntry entry2 = new DiaryEntry("Entry 2", EventSignificance.NORMAL);
        entry2.setUser(testUser);
        diaryRepository.save(entry1);
        diaryRepository.save(entry2);

        List<DiaryEntry> all = diaryRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDeleteById() {
        DiaryEntry entry = new DiaryEntry("To delete", EventSignificance.MINOR);
        entry.setUser(testUser);
        diaryRepository.save(entry);

        Optional<DiaryEntry> found = diaryRepository.findById(entry.getId());
        assertTrue(found.isPresent());

        diaryRepository.deleteById(entry.getId());

        found = diaryRepository.findById(entry.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> diaryRepository.deleteById("nonexistent-id"));
    }

    @Test
    void testFindByUserId() {
        DiaryEntry entry1 = new DiaryEntry("Entry 1", EventSignificance.MINOR);
        entry1.setUser(testUser);
        diaryRepository.save(entry1);

        User otherUser = new User("otherdiaryuser", "password123");
        userRepository.save(otherUser);
        DiaryEntry entry2 = new DiaryEntry("Entry 2", EventSignificance.NORMAL);
        entry2.setUser(otherUser);
        diaryRepository.save(entry2);

        List<DiaryEntry> userEntries = diaryRepository.findByUserId(testUser.getId());
        assertEquals(1, userEntries.size());
        assertEquals("Entry 1", userEntries.get(0).getDescription());
    }

    @Test
    void testFindByUserIdReturnsEmptyForNonExistentUser() {
        List<DiaryEntry> entries = diaryRepository.findByUserId("nonexistent-user-id");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testFindDistinctDescriptionsByUserId() {
        DiaryEntry entry1 = new DiaryEntry("Morning jog", EventSignificance.MINOR);
        entry1.setUser(testUser);
        DiaryEntry entry2 = new DiaryEntry("Morning jog", EventSignificance.NORMAL);
        entry2.setUser(testUser);
        entry2.setEventDate(LocalDate.now().minusDays(1));
        DiaryEntry entry3 = new DiaryEntry("Team lunch", EventSignificance.NORMAL);
        entry3.setUser(testUser);
        diaryRepository.save(entry1);
        diaryRepository.save(entry2);
        diaryRepository.save(entry3);

        List<String> descriptions = diaryRepository.findDistinctDescriptionsByUserId(testUser.getId());
        assertEquals(2, descriptions.size());
        assertEquals("Morning jog", descriptions.get(0));
        assertEquals("Team lunch", descriptions.get(1));
    }

    @Test
    void testFindDistinctDescriptionsByUserIdReturnsEmptyForNonExistentUser() {
        List<String> descriptions = diaryRepository.findDistinctDescriptionsByUserId("nonexistent-user-id");
        assertTrue(descriptions.isEmpty());
    }

    @Test
    void testEventDatePersisted() {
        LocalDate specificDate = LocalDate.of(2024, 6, 15);
        DiaryEntry entry = new DiaryEntry("Test event", EventSignificance.MAJOR, specificDate);
        entry.setUser(testUser);
        diaryRepository.save(entry);

        Optional<DiaryEntry> found = diaryRepository.findById(entry.getId());
        assertTrue(found.isPresent());
        assertEquals(specificDate, found.get().getEventDate());
    }
}
