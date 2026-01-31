package de.idrinth.habitevaluator.shared.api;

import com.google.gson.reflect.TypeToken;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Remote API implementation of UserRepository.
 * Uses the auth API to represent the currently logged-in user.
 * User management is handled server-side; this provides read access
 * to the authenticated user's information.
 */
public class RemoteUserRepository implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(RemoteUserRepository.class);
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final ApiClient apiClient;
    private User cachedUser;

    public RemoteUserRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public User save(User user) {
        // User creation/modification is managed server-side
        logger.debug("Save operation not supported for remote users, returning as-is");
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        User user = getCurrentUser();
        if (user != null && user.getId().equals(id)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        User user = getCurrentUser();
        if (user != null && user.getUsername().equals(username)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // Not supported via remote API
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        User user = getCurrentUser();
        if (user != null) {
            return List.of(user);
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteById(String id) {
        logger.debug("Delete operation not supported for remote users");
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    private User getCurrentUser() {
        if (cachedUser != null) {
            return cachedUser;
        }
        try {
            Map<String, Object> response = apiClient.get("/api/auth/me", MAP_TYPE);
            if (Boolean.TRUE.equals(response.get("success"))) {
                User user = new User();
                user.setId((String) response.get("userId"));
                user.setUsername((String) response.get("username"));
                cachedUser = user;
                return user;
            }
        } catch (IOException e) {
            logger.error("Failed to get current user from remote API", e);
        }
        return null;
    }

    /**
     * Clears the cached user, forcing a re-fetch on next access.
     */
    public void clearCache() {
        cachedUser = null;
    }
}
