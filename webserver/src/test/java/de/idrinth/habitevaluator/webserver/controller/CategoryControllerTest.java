package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    private HabitCategoryRepository habitCategoryRepository;
    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private StatsCacheService statsCacheService;
    private CategoryController controller;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        habitCategoryRepository = mock(HabitCategoryRepository.class);
        habitRepository = mock(HabitRepository.class);
        userRepository = mock(UserRepository.class);
        statsCacheService = mock(StatsCacheService.class);
        controller = new CategoryController(habitCategoryRepository, habitRepository, userRepository, statsCacheService);
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

    @Test
    void testUpdateCategorySuccess() {
        HabitCategory existing = new HabitCategory("Old Name", "Old desc", "#000000");
        existing.setUser(testUser);
        when(habitCategoryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(habitCategoryRepository.save(any(HabitCategory.class))).thenAnswer(i -> i.getArgument(0));

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "New Name";
        request.description = "New desc";
        request.color = "#FF0000";

        ResponseEntity<HabitCategory> response = controller.updateCategory(existing.getId(), request, session);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("New Name", response.getBody().getName());
        assertEquals("New desc", response.getBody().getDescription());
        assertEquals("#FF0000", response.getBody().getColor());
    }

    @Test
    void testUpdateCategoryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();
        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "Test";

        ResponseEntity<HabitCategory> response = controller.updateCategory("some-id", request, unauthSession);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateCategoryNotFound() {
        when(habitCategoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "Test";

        ResponseEntity<HabitCategory> response = controller.updateCategory("nonexistent", request, session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateCategoryWrongUser() {
        User otherUser = new User("other", "pass");
        HabitCategory existing = new HabitCategory("Cat", "desc", "#000");
        existing.setUser(otherUser);
        when(habitCategoryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "Test";

        ResponseEntity<HabitCategory> response = controller.updateCategory(existing.getId(), request, session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateCategoryBlankName() {
        HabitCategory existing = new HabitCategory("Cat", "desc", "#000");
        existing.setUser(testUser);
        when(habitCategoryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        CategoryController.CreateCategoryRequest request = new CategoryController.CreateCategoryRequest();
        request.name = "";

        ResponseEntity<HabitCategory> response = controller.updateCategory(existing.getId(), request, session);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testDeleteCategoryEmptyNoHabits() {
        HabitCategory existing = new HabitCategory("Cat", "desc", "#000");
        existing.setUser(testUser);
        when(habitCategoryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of());

        ResponseEntity<Void> response = controller.deleteCategory(existing.getId(), null, false, session);

        assertEquals(204, response.getStatusCode().value());
        verify(habitCategoryRepository).deleteById(existing.getId());
    }

    @Test
    void testDeleteCategoryUnauthenticated() {
        MockHttpSession unauthSession = new MockHttpSession();

        ResponseEntity<Void> response = controller.deleteCategory("some-id", null, false, unauthSession);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDeleteCategoryNotFound() {
        when(habitCategoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteCategory("nonexistent", null, false, session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteCategoryWrongUser() {
        User otherUser = new User("other", "pass");
        HabitCategory existing = new HabitCategory("Cat", "desc", "#000");
        existing.setUser(otherUser);
        when(habitCategoryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = controller.deleteCategory(existing.getId(), null, false, session);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteCategoryWithHabitsReassign() {
        HabitCategory source = new HabitCategory("Source", "desc", "#000");
        source.setUser(testUser);
        HabitCategory target = new HabitCategory("Target", "desc", "#FFF");
        target.setUser(testUser);

        Habit habit = new Habit("Test Habit", "desc");
        habit.setCategoryId(source.getId());
        habit.setUser(testUser);

        when(habitCategoryRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(habitCategoryRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>(List.of(habit)));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Void> response = controller.deleteCategory(source.getId(), target.getId(), false, session);

        assertEquals(204, response.getStatusCode().value());
        verify(habitRepository).save(habit);
        assertEquals(target.getId(), habit.getCategoryId());
        verify(habitCategoryRepository).deleteById(source.getId());
    }

    @Test
    void testDeleteCategoryWithHabitsReassignTargetNotFound() {
        HabitCategory source = new HabitCategory("Source", "desc", "#000");
        source.setUser(testUser);

        Habit habit = new Habit("Test Habit", "desc");
        habit.setCategoryId(source.getId());
        habit.setUser(testUser);

        when(habitCategoryRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(habitCategoryRepository.findById("nonexistent")).thenReturn(Optional.empty());
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(habit));

        ResponseEntity<Void> response = controller.deleteCategory(source.getId(), "nonexistent", false, session);

        assertEquals(400, response.getStatusCode().value());
        verify(habitCategoryRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteCategoryWithHabitsReassignTargetWrongUser() {
        HabitCategory source = new HabitCategory("Source", "desc", "#000");
        source.setUser(testUser);
        User otherUser = new User("other", "pass");
        HabitCategory target = new HabitCategory("Target", "desc", "#FFF");
        target.setUser(otherUser);

        Habit habit = new Habit("Test Habit", "desc");
        habit.setCategoryId(source.getId());
        habit.setUser(testUser);

        when(habitCategoryRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(habitCategoryRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(habit));

        ResponseEntity<Void> response = controller.deleteCategory(source.getId(), target.getId(), false, session);

        assertEquals(400, response.getStatusCode().value());
        verify(habitCategoryRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteCategoryWithHabitsNoConfirm() {
        HabitCategory source = new HabitCategory("Source", "desc", "#000");
        source.setUser(testUser);

        Habit habit = new Habit("Test Habit", "desc");
        habit.setCategoryId(source.getId());
        habit.setUser(testUser);

        when(habitCategoryRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(List.of(habit));

        ResponseEntity<Void> response = controller.deleteCategory(source.getId(), null, false, session);

        assertEquals(409, response.getStatusCode().value());
        verify(habitCategoryRepository, never()).deleteById(any());
        verify(habitRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteCategoryWithHabitsConfirmed() {
        HabitCategory source = new HabitCategory("Source", "desc", "#000");
        source.setUser(testUser);

        Habit habit = new Habit("Test Habit", "desc");
        habit.setCategoryId(source.getId());
        habit.setUser(testUser);

        when(habitCategoryRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(habitRepository.findByUserId(testUser.getId())).thenReturn(new ArrayList<>(List.of(habit)));

        ResponseEntity<Void> response = controller.deleteCategory(source.getId(), null, true, session);

        assertEquals(204, response.getStatusCode().value());
        verify(habitRepository).deleteById(habit.getId());
        verify(habitCategoryRepository).deleteById(source.getId());
    }
}
