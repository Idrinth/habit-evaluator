package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    private HabitCategoryRepository habitCategoryRepository;
    private UserRepository userRepository;
    private CategoryController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitCategoryRepository = mock(HabitCategoryRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new CategoryController(habitCategoryRepository, userRepository);
        session = new MockHttpSession();
        testUser = new User("testuser", "password");
        session.setAttribute("userId", testUser.getId());
    }

    @Test
    void testListCategoriesUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<List<HabitCategory>> response = controller.listCategories(unauthSession);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testListCategoriesSuccess() {
        List<HabitCategory> categories = List.of(new HabitCategory("Health", "Health habits", "#00FF00"));
        when(habitCategoryRepository.findByUserId(testUser.getId())).thenReturn(categories);

        ResponseEntity<List<HabitCategory>> response = controller.listCategories(session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Health", response.getBody().get(0).getName());
    }

    @Test
    void testCreateCategorySuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(habitCategoryRepository.save(any(HabitCategory.class))).thenAnswer(i -> i.getArgument(0));

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "Fitness";
        request.description = "Fitness habits";
        request.color = "#FF0000";

        ResponseEntity<HabitCategory> response = controller.createCategory(request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Fitness", response.getBody().getName());
        assertEquals("#FF0000", response.getBody().getColor());
    }

    @Test
    void testCreateCategoryBlankName() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "";
        request.description = "desc";

        ResponseEntity<HabitCategory> response = controller.createCategory(request, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateCategoryNullName() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = null;

        ResponseEntity<HabitCategory> response = controller.createCategory(request, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateCategoryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "Test";

        ResponseEntity<HabitCategory> response = controller.createCategory(request, unauthSession);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateCategoryUserNotFound() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "Test";

        ResponseEntity<HabitCategory> response = controller.createCategory(request, session);

        assertEquals(401, response.getStatusCode().value());
    }
}
