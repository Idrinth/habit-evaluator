package de.idrinth.habitevaluator.desktop.persistence;

import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JpaTransactionHelperTest extends H2RepositoryTestBase {

    @BeforeEach
    void setUp() {
        // Clean up users between tests
        for (User user : JpaTransactionHelper.findAll(User.class, "User")) {
            JpaTransactionHelper.deleteById(User.class, user.getId());
        }
    }

    @Test
    void testExecuteInTransactionReturnsResult() {
        String result = JpaTransactionHelper.executeInTransaction(em -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void testExecuteInTransactionCommitsOnSuccess() {
        User user = new User("txuser", "password");
        JpaTransactionHelper.executeInTransaction(em -> {
            em.persist(user);
            return user;
        });

        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertTrue(found.isPresent());
        assertEquals("txuser", found.get().getUsername());
    }

    @Test
    void testExecuteInTransactionRollsBackOnException() {
        User user = new User("rollbackuser", "password");
        try {
            JpaTransactionHelper.executeInTransaction(em -> {
                em.persist(user);
                throw new RuntimeException("Test rollback");
            });
        } catch (RuntimeException e) {
            assertEquals("Test rollback", e.getMessage());
        }

        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testExecuteInTransactionRethrowsException() {
        assertThrows(RuntimeException.class, () ->
                JpaTransactionHelper.executeInTransaction(em -> {
                    throw new RuntimeException("Test exception");
                })
        );
    }

    @Test
    void testExecuteInTransactionVoidCommits() {
        User user = new User("voiduser", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> em.persist(user));

        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void testExecuteInTransactionVoidRollsBackOnException() {
        User user = new User("voidrollback", "password");
        try {
            JpaTransactionHelper.executeInTransactionVoid(em -> {
                em.persist(user);
                throw new RuntimeException("Void rollback");
            });
        } catch (RuntimeException ignored) {
        }

        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testExecuteReadOnlyReturnsResult() {
        User user = new User("readonly", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> em.persist(user));

        User found = JpaTransactionHelper.executeReadOnly(em -> em.find(User.class, user.getId()));
        assertNotNull(found);
        assertEquals("readonly", found.getUsername());
    }

    @Test
    void testExecuteReadOnlyWithNoResults() {
        User found = JpaTransactionHelper.executeReadOnly(em -> em.find(User.class, "nonexistent"));
        assertNull(found);
    }

    @Test
    void testSaveOrUpdatePersistsNewEntity() {
        User user = new User("newentity", "password");
        User saved = JpaTransactionHelper.saveOrUpdate(User.class, user, User::getId);

        assertNotNull(saved);
        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertTrue(found.isPresent());
        assertEquals("newentity", found.get().getUsername());
    }

    @Test
    void testSaveOrUpdateMergesExistingEntity() {
        User user = new User("mergeuser", "password");
        JpaTransactionHelper.saveOrUpdate(User.class, user, User::getId);

        user.setUsername("mergedname");
        JpaTransactionHelper.saveOrUpdate(User.class, user, User::getId);

        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertTrue(found.isPresent());
        assertEquals("mergedname", found.get().getUsername());
    }

    @Test
    void testFindByIdReturnsEntity() {
        User user = new User("findme", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> em.persist(user));

        Optional<User> found = JpaTransactionHelper.findById(User.class, user.getId());
        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getUsername());
    }

    @Test
    void testFindByIdReturnsEmptyForNonExistent() {
        Optional<User> found = JpaTransactionHelper.findById(User.class, "does-not-exist");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllReturnsAllEntities() {
        User user1 = new User("all1", "password");
        User user2 = new User("all2", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> {
            em.persist(user1);
            em.persist(user2);
        });

        List<User> all = JpaTransactionHelper.findAll(User.class, "User");
        assertEquals(2, all.size());
    }

    @Test
    void testFindAllReturnsEmptyListWhenNoEntities() {
        List<User> all = JpaTransactionHelper.findAll(User.class, "User");
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindByParameterReturnsMatchingEntities() {
        User user1 = new User("paramuser", "password");
        User user2 = new User("otheruser", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> {
            em.persist(user1);
            em.persist(user2);
        });

        List<User> found = JpaTransactionHelper.findByParameter(
                User.class,
                "SELECT u FROM User u WHERE u.username = :name",
                "name",
                "paramuser"
        );
        assertEquals(1, found.size());
        assertEquals("paramuser", found.get(0).getUsername());
    }

    @Test
    void testFindByParameterReturnsEmptyForNoMatch() {
        List<User> found = JpaTransactionHelper.findByParameter(
                User.class,
                "SELECT u FROM User u WHERE u.username = :name",
                "name",
                "nobody"
        );
        assertTrue(found.isEmpty());
    }

    @Test
    void testDeleteByIdRemovesEntity() {
        User user = new User("deleteme", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> em.persist(user));

        assertTrue(JpaTransactionHelper.existsById(User.class, user.getId()));
        JpaTransactionHelper.deleteById(User.class, user.getId());
        assertFalse(JpaTransactionHelper.existsById(User.class, user.getId()));
    }

    @Test
    void testDeleteByIdNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> JpaTransactionHelper.deleteById(User.class, "nonexistent"));
    }

    @Test
    void testExistsByIdReturnsTrueForExisting() {
        User user = new User("exists", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> em.persist(user));

        assertTrue(JpaTransactionHelper.existsById(User.class, user.getId()));
    }

    @Test
    void testExistsByIdReturnsFalseForNonExistent() {
        assertFalse(JpaTransactionHelper.existsById(User.class, "missing"));
    }

    @Test
    void testFindSingleByParameterReturnsMatchingEntity() {
        User user = new User("singleuser", "password");
        JpaTransactionHelper.executeInTransactionVoid(em -> em.persist(user));

        Optional<User> found = JpaTransactionHelper.findSingleByParameter(
                User.class,
                "SELECT u FROM User u WHERE u.username = :name",
                "name",
                "singleuser"
        );
        assertTrue(found.isPresent());
        assertEquals("singleuser", found.get().getUsername());
    }

    @Test
    void testFindSingleByParameterReturnsEmptyForNoMatch() {
        Optional<User> found = JpaTransactionHelper.findSingleByParameter(
                User.class,
                "SELECT u FROM User u WHERE u.username = :name",
                "name",
                "ghost"
        );
        assertFalse(found.isPresent());
    }

    @Test
    void testCountByParameterReturnsCorrectCount() {
        User user1 = new User("count1", "samepass");
        User user2 = new User("count2", "samepass");
        User user3 = new User("count3", "different");
        JpaTransactionHelper.executeInTransactionVoid(em -> {
            em.persist(user1);
            em.persist(user2);
            em.persist(user3);
        });

        long count = JpaTransactionHelper.countByParameter(
                "SELECT COUNT(u) FROM User u WHERE u.password = :pw",
                "pw",
                "samepass"
        );
        assertEquals(2, count);
    }

    @Test
    void testCountByParameterReturnsZeroForNoMatch() {
        long count = JpaTransactionHelper.countByParameter(
                "SELECT COUNT(u) FROM User u WHERE u.password = :pw",
                "pw",
                "noone"
        );
        assertEquals(0, count);
    }
}
