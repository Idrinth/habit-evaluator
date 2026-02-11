package de.idrinth.habitevaluator.shared.api;

import de.idrinth.habitevaluator.shared.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RemoteUserRepositoryTest {

    private StubApiClient stubApiClient;
    private RemoteUserRepository repository;

    @BeforeEach
    void setUp() {
        stubApiClient = new StubApiClient();
        repository = new RemoteUserRepository(stubApiClient);
    }

    @Test
    void testSaveReturnsUserAsIs() {
        User user = new User("testuser", "password");
        User result = repository.save(user);
        assertSame(user, result);
    }

    @Test
    void testFindByIdReturnsUserWhenIdMatches() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        Optional<User> found = repository.findById("user-123");
        assertTrue(found.isPresent());
        assertEquals("user-123", found.get().getId());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByIdReturnsEmptyWhenIdDoesNotMatch() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        Optional<User> found = repository.findById("different-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdReturnsEmptyOnIOException() {
        stubApiClient.setGetException(new IOException("Network error"));

        Optional<User> found = repository.findById("user-123");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdReturnsEmptyWhenAuthFails() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", false);
        stubApiClient.setGetResponse(authResponse);

        Optional<User> found = repository.findById("user-123");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUsernameReturnsUserWhenUsernameMatches() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        Optional<User> found = repository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByUsernameReturnsEmptyWhenUsernameDoesNotMatch() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        Optional<User> found = repository.findByUsername("otheruser");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmailAlwaysReturnsEmpty() {
        Optional<User> found = repository.findByEmail("test@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllReturnsCurrentUser() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        List<User> users = repository.findAll();
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
    }

    @Test
    void testFindAllReturnsEmptyWhenNoCurrentUser() {
        stubApiClient.setGetException(new IOException("Network error"));

        List<User> users = repository.findAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void testDeleteByIdIsNoOp() {
        assertDoesNotThrow(() -> repository.deleteById("any-id"));
    }

    @Test
    void testExistsByIdReturnsTrueWhenIdMatches() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        assertTrue(repository.existsById("user-123"));
    }

    @Test
    void testExistsByIdReturnsFalseWhenIdDoesNotMatch() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        assertFalse(repository.existsById("different-id"));
    }

    @Test
    void testExistsByUsernameReturnsTrueWhenMatches() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        assertTrue(repository.existsByUsername("testuser"));
    }

    @Test
    void testExistsByUsernameReturnsFalseWhenDoesNotMatch() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        assertFalse(repository.existsByUsername("otheruser"));
    }

    @Test
    void testCachingBehavior() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        // First call fetches from API
        Optional<User> first = repository.findById("user-123");
        assertTrue(first.isPresent());
        assertEquals(1, stubApiClient.getCallCount());

        // Second call should use cache (no additional API call)
        Optional<User> second = repository.findById("user-123");
        assertTrue(second.isPresent());
        assertEquals(1, stubApiClient.getCallCount());
    }

    @Test
    void testClearCacheForcesFetchOnNextAccess() {
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("success", true);
        authResponse.put("userId", "user-123");
        authResponse.put("username", "testuser");
        stubApiClient.setGetResponse(authResponse);

        // First call
        repository.findById("user-123");
        assertEquals(1, stubApiClient.getCallCount());

        // Clear cache
        repository.clearCache();

        // Next call should fetch from API again
        repository.findById("user-123");
        assertEquals(2, stubApiClient.getCallCount());
    }

    /**
     * Stub ApiClient for testing RemoteUserRepository without network calls.
     */
    static class StubApiClient extends ApiClient {

        private Object getResponse;
        private IOException getException;
        private int getCallCount;

        StubApiClient() {
            super("http://localhost:8080");
        }

        void setGetResponse(Object response) {
            this.getResponse = response;
            this.getException = null;
        }

        void setGetException(IOException exception) {
            this.getException = exception;
            this.getResponse = null;
        }

        int getCallCount() {
            return getCallCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(String path, Type responseType) throws IOException {
            getCallCount++;
            if (getException != null) {
                throw getException;
            }
            return (T) getResponse;
        }
    }
}
