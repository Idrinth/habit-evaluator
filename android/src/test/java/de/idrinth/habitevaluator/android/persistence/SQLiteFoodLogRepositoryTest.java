package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.FoodLog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteFoodLogRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteFoodLogRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteFoodLogRepository(dbHelper);
    }

    @Test
    void testSaveCallsInsert() {
        FoodLog entry = new FoodLog(25.0, 300, LocalDateTime.of(2024, 6, 15, 12, 0), "Rice, Chicken");

        repository.save(entry);

        verify(db).insertWithOnConflict(eq("food_logs"), isNull(), any(), eq(5));
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM food_logs WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createFoodLogCursor("f1", 25.5, 350, "2024-06-15T12:00:00", "Rice, Chicken", "Lunch", null, null);
        when(db.rawQuery(eq("SELECT * FROM food_logs WHERE id = ?"), eq(new String[]{"f1"}))).thenReturn(cursor);

        Optional<FoodLog> result = repository.findById("f1");

        assertTrue(result.isPresent());
        assertEquals(25.5, result.get().getCarbohydrates(), 0.01);
        assertEquals(Integer.valueOf(350), result.get().getKcal());
        assertEquals("Rice, Chicken", result.get().getFoodItems());
        assertEquals("Lunch", result.get().getNotes());
    }

    @Test
    void testFindByIdWithNullCarbohydratesAndKcal() {
        Cursor cursor = createFoodLogCursor("f1", null, null, "2024-06-15T12:00:00", "Snack", null, null, null);
        when(db.rawQuery(eq("SELECT * FROM food_logs WHERE id = ?"), eq(new String[]{"f1"}))).thenReturn(cursor);

        Optional<FoodLog> result = repository.findById("f1");

        assertTrue(result.isPresent());
        assertNull(result.get().getCarbohydrates());
        assertNull(result.get().getKcal());
    }

    @Test
    void testFindByIdWithUser() {
        Cursor cursor = createFoodLogCursor("f1", 10.0, 200, "2024-06-15T12:00:00", "Salad", null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM food_logs WHERE id = ?"), eq(new String[]{"f1"}))).thenReturn(cursor);

        Optional<FoodLog> result = repository.findById("f1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM food_logs"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("f1");

        verify(db).delete(eq("food_logs"), eq("id = ?"), eq(new String[]{"f1"}));
    }

    @Test
    void testExistsByIdTrue() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(db.rawQuery(eq("SELECT 1 FROM food_logs WHERE id = ?"), eq(new String[]{"f1"}))).thenReturn(cursor);

        assertTrue(repository.existsById("f1"));
    }

    @Test
    void testExistsByIdFalse() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT 1 FROM food_logs WHERE id = ?"), eq(new String[]{"f1"}))).thenReturn(cursor);

        assertFalse(repository.existsById("f1"));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM food_logs WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    void testSaveReturnsEntry() {
        FoodLog entry = new FoodLog();
        FoodLog result = repository.save(entry);
        assertSame(entry, result);
    }

    @Test
    void testFindAllWithMultipleEntries() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, true, false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("carbohydrates")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("kcal")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("date_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("food_items")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(8);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);

        when(cursor.getString(0)).thenReturn("f1", "f2");
        when(cursor.getString(3)).thenReturn("2024-06-15T12:00:00");
        when(cursor.getString(4)).thenReturn("Rice", "Pasta");
        when(cursor.getString(5)).thenReturn("2024-06-15T10:00:00");

        when(db.rawQuery(eq("SELECT * FROM food_logs"), isNull())).thenReturn(cursor);

        List<FoodLog> result = repository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindByUserIdWithResults() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("carbohydrates")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("kcal")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("date_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("food_items")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(8);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);
        when(cursor.isNull(7)).thenReturn(false);
        when(cursor.isNull(8)).thenReturn(false);

        when(cursor.getString(0)).thenReturn("f1");
        when(cursor.getString(3)).thenReturn("2024-06-15T12:00:00");
        when(cursor.getString(4)).thenReturn("Rice");
        when(cursor.getString(5)).thenReturn("2024-06-15T10:00:00");
        when(cursor.getString(7)).thenReturn("u1");
        when(cursor.getString(8)).thenReturn("testuser");

        when(db.rawQuery(startsWith("SELECT * FROM food_logs WHERE user_id"), any())).thenReturn(cursor);

        List<FoodLog> result = repository.findByUserId("u1");
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getUser());
        assertEquals("u1", result.get(0).getUser().getId());
    }

    @Test
    void testSaveWithNullFieldsDoesNotThrow() {
        FoodLog entry = new FoodLog();
        entry.setCarbohydrates(null);
        entry.setKcal(null);
        entry.setFoodItems("Snack");

        repository.save(entry);
        verify(db).insertWithOnConflict(eq("food_logs"), isNull(), any(), eq(5));
    }

    @Test
    void testSaveWithUserAttached() {
        FoodLog entry = new FoodLog(25.0, 300, LocalDateTime.of(2024, 6, 15, 12, 0), "Rice");
        de.idrinth.habitevaluator.shared.model.User user = new de.idrinth.habitevaluator.shared.model.User();
        user.setId("u1");
        user.setUsername("testuser");
        entry.setUser(user);

        repository.save(entry);
        verify(db).insertWithOnConflict(eq("food_logs"), isNull(), any(), eq(5));
    }

    @Test
    void testFindByIdWithNullDateTimeFromCursor() {
        // When date_time and created_at are null in the cursor,
        // the FoodLog constructor's defaults (LocalDateTime.now()) remain.
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("carbohydrates")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("kcal")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("date_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("food_items")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(8);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);

        when(cursor.getString(0)).thenReturn("f1");
        when(cursor.getString(4)).thenReturn("Snack");

        when(db.rawQuery(eq("SELECT * FROM food_logs WHERE id = ?"), eq(new String[]{"f1"}))).thenReturn(cursor);

        Optional<FoodLog> result = repository.findById("f1");
        assertTrue(result.isPresent());
        assertEquals("f1", result.get().getId());
        assertEquals("Snack", result.get().getFoodItems());
        assertNull(result.get().getCarbohydrates());
        assertNull(result.get().getKcal());
        assertNull(result.get().getNotes());
        assertNull(result.get().getUser());
    }

    private Cursor createFoodLogCursor(String id, Double carbohydrates, Integer kcal, String dateTime, String foodItems, String notes, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("carbohydrates")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("kcal")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("date_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("food_items")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(8);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(3)).thenReturn(dateTime);
        when(cursor.getString(4)).thenReturn(foodItems);
        when(cursor.getString(5)).thenReturn("2024-06-15T10:00:00");

        if (carbohydrates != null) {
            when(cursor.isNull(1)).thenReturn(false);
            when(cursor.getDouble(1)).thenReturn(carbohydrates);
        }
        if (kcal != null) {
            when(cursor.isNull(2)).thenReturn(false);
            when(cursor.getInt(2)).thenReturn(kcal);
        }
        if (notes != null) {
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.getString(6)).thenReturn(notes);
        }
        if (userId != null) {
            when(cursor.isNull(7)).thenReturn(false);
            when(cursor.isNull(8)).thenReturn(false);
            when(cursor.getString(7)).thenReturn(userId);
            when(cursor.getString(8)).thenReturn(userName);
        }

        return cursor;
    }

    private Cursor createEmptyCursor() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(cursor.moveToNext()).thenReturn(false);
        return cursor;
    }
}
