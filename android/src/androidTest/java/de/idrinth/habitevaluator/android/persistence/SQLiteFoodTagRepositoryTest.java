package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.idrinth.habitevaluator.shared.model.FoodLog;
import de.idrinth.habitevaluator.shared.model.FoodTag;
import de.idrinth.habitevaluator.shared.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SQLiteFoodTagRepositoryTest {

    private SQLiteFoodTagRepository repository;
    private SQLiteFoodLogRepository foodLogRepository;
    private SQLiteHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("habit_evaluator.db");
        dbHelper = SQLiteHelper.getInstance(context);
        repository = new SQLiteFoodTagRepository(dbHelper);
        foodLogRepository = new SQLiteFoodLogRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("habit_evaluator.db");
    }

    @Test
    public void testSaveAndFindById() {
        FoodTag tag = createTag("ft-1", "Vegetarian", "u1");
        repository.save(tag);

        Optional<FoodTag> found = repository.findById("ft-1");
        assertTrue(found.isPresent());
        assertEquals("Vegetarian", found.get().getName());
        assertEquals("vegetarian", found.get().getNameLower());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<FoodTag> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteById() {
        repository.save(createTag("to-delete", "Temp", "u1"));
        assertTrue(repository.findById("to-delete").isPresent());

        repository.deleteById("to-delete");
        assertFalse(repository.findById("to-delete").isPresent());
    }

    @Test
    public void testFindByUserId() {
        repository.save(createTag("ft-1", "Vegetarian", "u1"));
        repository.save(createTag("ft-2", "Vegan", "u1"));
        repository.save(createTag("ft-3", "Gluten-Free", "u2"));

        List<FoodTag> user1Tags = repository.findByUserId("u1");
        assertEquals(2, user1Tags.size());

        List<FoodTag> user2Tags = repository.findByUserId("u2");
        assertEquals(1, user2Tags.size());
    }

    @Test
    public void testFindByNameLowerAndUserId() {
        repository.save(createTag("ft-1", "Vegetarian", "u1"));

        Optional<FoodTag> found = repository.findByNameLowerAndUserId("vegetarian", "u1");
        assertTrue(found.isPresent());
        assertEquals("Vegetarian", found.get().getName());
    }

    @Test
    public void testFindByNameLowerAndUserIdNotFound() {
        repository.save(createTag("ft-1", "Vegetarian", "u1"));

        Optional<FoodTag> wrongUser = repository.findByNameLowerAndUserId("vegetarian", "u2");
        assertFalse(wrongUser.isPresent());

        Optional<FoodTag> wrongName = repository.findByNameLowerAndUserId("vegan", "u1");
        assertFalse(wrongName.isPresent());
    }

    @Test
    public void testLinkAndFindTagsByFoodLogId() {
        FoodTag tag1 = createTag("ft-1", "Vegetarian", "u1");
        FoodTag tag2 = createTag("ft-2", "Organic", "u1");
        repository.save(tag1);
        repository.save(tag2);

        FoodLog log = new FoodLog();
        log.setId("fl-1");
        log.setFoodItems("Salad");
        log.setDateTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId("u1");
        user.setUsername("user_u1");
        user.setPassword("placeholder");
        log.setUser(user);
        foodLogRepository.save(log);

        repository.linkTagToFoodLog("fl-1", "ft-1");
        repository.linkTagToFoodLog("fl-1", "ft-2");

        List<FoodTag> tags = repository.findTagsByFoodLogId("fl-1");
        assertEquals(2, tags.size());
    }

    @Test
    public void testUnlinkAllTagsFromFoodLog() {
        FoodTag tag = createTag("ft-1", "Vegetarian", "u1");
        repository.save(tag);

        FoodLog log = new FoodLog();
        log.setId("fl-1");
        log.setFoodItems("Salad");
        log.setDateTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId("u1");
        user.setUsername("user_u1");
        user.setPassword("placeholder");
        log.setUser(user);
        foodLogRepository.save(log);

        repository.linkTagToFoodLog("fl-1", "ft-1");
        assertEquals(1, repository.findTagsByFoodLogId("fl-1").size());

        repository.unlinkAllTagsFromFoodLog("fl-1");
        assertEquals(0, repository.findTagsByFoodLogId("fl-1").size());
    }

    @Test
    public void testLinkTagDuplicateIgnored() {
        FoodTag tag = createTag("ft-1", "Vegetarian", "u1");
        repository.save(tag);

        FoodLog log = new FoodLog();
        log.setId("fl-1");
        log.setFoodItems("Salad");
        log.setDateTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setId("u1");
        user.setUsername("user_u1");
        user.setPassword("placeholder");
        log.setUser(user);
        foodLogRepository.save(log);

        repository.linkTagToFoodLog("fl-1", "ft-1");
        repository.linkTagToFoodLog("fl-1", "ft-1");

        List<FoodTag> tags = repository.findTagsByFoodLogId("fl-1");
        assertEquals(1, tags.size());
    }

    @Test
    public void testUpdateExistingTag() {
        repository.save(createTag("ft-update", "Vegetarian", "u1"));

        FoodTag updated = createTag("ft-update", "Vegan", "u1");
        repository.save(updated);

        Optional<FoodTag> found = repository.findById("ft-update");
        assertTrue(found.isPresent());
        assertEquals("Vegan", found.get().getName());
        assertEquals("vegan", found.get().getNameLower());
    }

    @Test
    public void testSavePreservesUser() {
        FoodTag tag = createTag("ft-user", "Tag", "u1");
        repository.save(tag);

        Optional<FoodTag> found = repository.findById("ft-user");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals("u1", found.get().getUser().getId());
    }

    private FoodTag createTag(String id, String name, String userId) {
        FoodTag tag = new FoodTag();
        tag.setId(id);
        tag.setName(name);
        tag.setNameLower(name.toLowerCase());
        User user = new User();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setPassword("placeholder");
        tag.setUser(user);
        return tag;
    }
}
