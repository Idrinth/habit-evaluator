package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
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
class JpaDiaryReferenceRepositoryTest {

    @Autowired
    private JpaDiaryReferenceRepository diaryReferenceRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("refuser", "password123", "refuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveAndFindById() {
        DiaryReference ref = new DiaryReference("Morning workout");
        ref.setUser(testUser);
        DiaryReference saved = diaryReferenceRepository.save(ref);

        Optional<DiaryReference> found = diaryReferenceRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Morning workout", found.get().getDescription());
        assertEquals("morning workout", found.get().getDescriptionLower());
    }

    @Test
    void testFindByUserId() {
        DiaryReference ref1 = new DiaryReference("Meeting");
        ref1.setUser(testUser);
        diaryReferenceRepository.save(ref1);

        DiaryReference ref2 = new DiaryReference("Exercise");
        ref2.setUser(testUser);
        diaryReferenceRepository.save(ref2);

        List<DiaryReference> refs = diaryReferenceRepository.findByUserId(testUser.getId());

        assertEquals(2, refs.size());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCase() {
        DiaryReference ref = new DiaryReference("Morning Routine");
        ref.setUser(testUser);
        diaryReferenceRepository.save(ref);

        Optional<DiaryReference> found = diaryReferenceRepository
                .findByUserIdAndDescriptionIgnoreCase(testUser.getId(), "morning routine");
        assertTrue(found.isPresent());
        assertEquals("Morning Routine", found.get().getDescription());

        Optional<DiaryReference> foundUpper = diaryReferenceRepository
                .findByUserIdAndDescriptionIgnoreCase(testUser.getId(), "MORNING ROUTINE");
        assertTrue(foundUpper.isPresent());
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseNotFound() {
        DiaryReference ref = new DiaryReference("Morning Routine");
        ref.setUser(testUser);
        diaryReferenceRepository.save(ref);

        Optional<DiaryReference> found = diaryReferenceRepository
                .findByUserIdAndDescriptionIgnoreCase(testUser.getId(), "evening routine");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindDistinctDescriptionsByUserId() {
        DiaryReference ref1 = new DiaryReference("Alpha Event");
        ref1.setUser(testUser);
        diaryReferenceRepository.save(ref1);

        DiaryReference ref2 = new DiaryReference("Beta Event");
        ref2.setUser(testUser);
        diaryReferenceRepository.save(ref2);

        List<String> descriptions = diaryReferenceRepository.findDistinctDescriptionsByUserId(testUser.getId());

        assertEquals(2, descriptions.size());
        assertEquals("Alpha Event", descriptions.get(0));
        assertEquals("Beta Event", descriptions.get(1));
    }

    @Test
    void testFindByUserIdEmptyForOtherUser() {
        DiaryReference ref = new DiaryReference("Test");
        ref.setUser(testUser);
        diaryReferenceRepository.save(ref);

        User otherUser = new User("otherrefuser", "password", "otherref@example.com");
        otherUser = userRepository.save(otherUser);

        List<DiaryReference> refs = diaryReferenceRepository.findByUserId(otherUser.getId());
        assertTrue(refs.isEmpty());
    }

    @Test
    void testDeleteById() {
        DiaryReference ref = new DiaryReference("To delete");
        ref.setUser(testUser);
        DiaryReference saved = diaryReferenceRepository.save(ref);

        diaryReferenceRepository.deleteById(saved.getId());

        assertFalse(diaryReferenceRepository.findById(saved.getId()).isPresent());
    }
}
