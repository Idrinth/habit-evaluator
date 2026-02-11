package de.idrinth.habitevaluator.shared.repository;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the default findOrCreate() method on the DiaryReferenceRepository interface.
 */
class DiaryReferenceRepositoryTest {

    private InMemoryDiaryReferenceRepository repository;
    private User testUser;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDiaryReferenceRepository();
        testUser = new User("testuser", "password");
    }

    @Test
    void testFindOrCreateReturnsExistingReferenceWhenFoundCaseInsensitive() {
        DiaryReference existing = new DiaryReference("Morning Jog");
        existing.setUser(testUser);
        repository.save(existing);

        DiaryReference result = repository.findOrCreate(testUser.getId(), "morning jog", () -> testUser);

        assertNotNull(result);
        assertEquals(existing.getId(), result.getId());
        assertEquals("Morning Jog", result.getDescription());
    }

    @Test
    void testFindOrCreateCreatesNewReferenceWhenNotFound() {
        DiaryReference result = repository.findOrCreate(testUser.getId(), "Evening Walk", () -> testUser);

        assertNotNull(result);
        assertEquals("Evening Walk", result.getDescription());
        assertNotNull(result.getUser());
        assertEquals(testUser.getId(), result.getUser().getId());
    }

    @Test
    void testFindOrCreateReturnsNullForNullDescription() {
        DiaryReference result = repository.findOrCreate(testUser.getId(), null, () -> testUser);
        assertNull(result);
    }

    @Test
    void testFindOrCreateReturnsNullForEmptyDescription() {
        DiaryReference result = repository.findOrCreate(testUser.getId(), "", () -> testUser);
        assertNull(result);
    }

    @Test
    void testFindOrCreatePreservesOriginalCase() {
        DiaryReference result = repository.findOrCreate(testUser.getId(), "UPPER CASE Event", () -> testUser);

        assertNotNull(result);
        assertEquals("UPPER CASE Event", result.getDescription());
    }

    @Test
    void testFindOrCreateDoesNotDuplicateExistingReference() {
        repository.findOrCreate(testUser.getId(), "Workout", () -> testUser);
        repository.findOrCreate(testUser.getId(), "WORKOUT", () -> testUser);
        repository.findOrCreate(testUser.getId(), "workout", () -> testUser);

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testFindOrCreateSavesNewReferenceToRepository() {
        assertEquals(0, repository.findAll().size());

        repository.findOrCreate(testUser.getId(), "New Entry", () -> testUser);

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testFindOrCreateUsesUserSupplierForNewReferences() {
        User suppliedUser = new User("supplied", "pass");
        DiaryReference result = repository.findOrCreate(testUser.getId(), "Test", () -> suppliedUser);

        assertNotNull(result);
        assertEquals(suppliedUser.getId(), result.getUser().getId());
    }

    @Test
    void testFindOrCreateDistinguishesBetweenUsers() {
        User otherUser = new User("otheruser", "password");
        DiaryReference ref1 = new DiaryReference("Shared Description");
        ref1.setUser(testUser);
        repository.save(ref1);

        // Different user same description should create a new reference
        DiaryReference result = repository.findOrCreate(otherUser.getId(), "Shared Description", () -> otherUser);

        assertNotNull(result);
        assertNotEquals(ref1.getId(), result.getId());
        assertEquals(2, repository.findAll().size());
    }

    /**
     * Simple in-memory implementation of DiaryReferenceRepository for testing the default method.
     */
    static class InMemoryDiaryReferenceRepository implements DiaryReferenceRepository {

        private final Map<String, DiaryReference> store = new ConcurrentHashMap<>();

        @Override
        public DiaryReference save(DiaryReference reference) {
            store.put(reference.getId(), reference);
            return reference;
        }

        @Override
        public Optional<DiaryReference> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<DiaryReference> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void deleteById(String id) {
            store.remove(id);
        }

        @Override
        public List<DiaryReference> findByUserId(String userId) {
            return store.values().stream()
                    .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<DiaryReference> findByUserIdAndDescriptionIgnoreCase(String userId, String description) {
            if (description == null) {
                return Optional.empty();
            }
            String descLower = description.toLowerCase();
            return store.values().stream()
                    .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                    .filter(r -> r.getDescriptionLower() != null && r.getDescriptionLower().equals(descLower))
                    .findFirst();
        }

        @Override
        public List<String> findDistinctDescriptionsByUserId(String userId) {
            return store.values().stream()
                    .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                    .map(DiaryReference::getDescription)
                    .filter(d -> d != null && !d.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
}
