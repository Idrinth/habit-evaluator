package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FoodLogTest {

    @Test
    void testDefaultConstructor() {
        FoodLog log = new FoodLog();
        assertNotNull(log.getId());
        assertEquals(36, log.getId().length());
        assertNotNull(log.getCreatedAt());
        assertNotNull(log.getDateTime());
        assertEquals("", log.getFoodItems());
        assertNotNull(log.getTags());
        assertTrue(log.getTags().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime dt = LocalDateTime.of(2025, 1, 15, 12, 0);
        FoodLog log = new FoodLog(30.0, 500, dt, "Rice, Chicken");
        assertEquals(30.0, log.getCarbohydrates());
        assertEquals(500, log.getKcal());
        assertEquals(dt, log.getDateTime());
        assertEquals("Rice, Chicken", log.getFoodItems());
    }

    @Test
    void testGetFoodItemListParsesCommaSeparated() {
        FoodLog log = new FoodLog(null, null, LocalDateTime.now(), "Rice, Chicken, Salad");
        List<String> items = log.getFoodItemList();
        assertEquals(3, items.size());
        assertEquals("Rice", items.get(0));
        assertEquals("Chicken", items.get(1));
        assertEquals("Salad", items.get(2));
    }

    @Test
    void testGetFoodItemListEmpty() {
        FoodLog log = new FoodLog();
        assertTrue(log.getFoodItemList().isEmpty());
    }

    @Test
    void testGetFoodItemListNull() {
        FoodLog log = new FoodLog();
        log.setFoodItems(null);
        assertTrue(log.getFoodItemList().isEmpty());
    }

    @Test
    void testSetFoodItemList() {
        FoodLog log = new FoodLog();
        log.setFoodItemList(Arrays.asList("Pasta", "Tomato Sauce", "Cheese"));
        assertEquals("Pasta, Tomato Sauce, Cheese", log.getFoodItems());
    }

    @Test
    void testSetFoodItemListEmpty() {
        FoodLog log = new FoodLog();
        log.setFoodItemList(List.of());
        assertEquals("", log.getFoodItems());
    }

    @Test
    void testSetFoodItemListNull() {
        FoodLog log = new FoodLog();
        log.setFoodItemList(null);
        assertEquals("", log.getFoodItems());
    }

    @Test
    void testTagsGetterSetter() {
        FoodLog log = new FoodLog();
        FoodTag tag1 = new FoodTag("Rice");
        FoodTag tag2 = new FoodTag("Chicken");
        Set<FoodTag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);
        log.setTags(tags);
        assertEquals(2, log.getTags().size());
        assertTrue(log.getTags().contains(tag1));
        assertTrue(log.getTags().contains(tag2));
    }

    @Test
    void testEqualsSameId() {
        FoodLog l1 = new FoodLog();
        FoodLog l2 = new FoodLog();
        l2.setId(l1.getId());
        assertEquals(l1, l2);
    }

    @Test
    void testEqualsDifferentId() {
        FoodLog l1 = new FoodLog();
        FoodLog l2 = new FoodLog();
        assertNotEquals(l1, l2);
    }

    @Test
    void testEqualsNull() {
        FoodLog l = new FoodLog();
        assertNotEquals(null, l);
    }

    @Test
    void testEqualsSameObject() {
        FoodLog l = new FoodLog();
        assertEquals(l, l);
    }

    @Test
    void testHashCodeConsistent() {
        FoodLog l1 = new FoodLog();
        FoodLog l2 = new FoodLog();
        l2.setId(l1.getId());
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    void testSetUser() {
        FoodLog log = new FoodLog();
        User user = new User("test", "pass");
        log.setUser(user);
        assertSame(user, log.getUser());
    }

    @Test
    void testNotes() {
        FoodLog log = new FoodLog();
        log.setNotes("Felt good after eating");
        assertEquals("Felt good after eating", log.getNotes());
    }
}
