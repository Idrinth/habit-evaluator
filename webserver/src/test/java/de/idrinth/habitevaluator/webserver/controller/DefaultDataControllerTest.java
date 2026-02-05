package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultDataControllerTest {

    private HabitCategoryRepository categoryRepository;
    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private DefaultDataController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(HabitCategoryRepository.class);
        habitRepository = mock(HabitRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new DefaultDataController(categoryRepository, habitRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testInitializeDefaultsUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<Map<String, Object>> response = controller.initializeDefaults(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testInitializeDefaultsUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.initializeDefaults(session);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testInitializeDefaultsSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = controller.initializeDefaults(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(true, response.getBody().get("success"));
    }
}
