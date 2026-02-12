package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SQLiteHabitRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteHabitRepository repository;

    @Before
    public void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteHabitRepository(dbHelper);
    }

    @Test
    public void testSaveCallsInsertWithTransaction() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        habit.setFrequencyType(FrequencyType.DAILY);

        repository.save(habit);

        verify(db).beginTransaction();
        verify(db).insertWithOnConflict(eq("habits"), isNull(), any(), eq(5));
        verify(db).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    public void testSaveDeletesAndReInsertsEntries() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        entry.setHabit(habit);
        habit.setEntries(new ArrayList<>());
        habit.getEntries().add(entry);

        repository.save(habit);

        verify(db).delete(eq("habit_entries"), eq("habit_id = ?"), eq(new String[]{habit.getId()}));
        verify(db).insertWithOnConflict(eq("habit_entries"), isNull(), any(), eq(5));
    }

    @Test
    public void testSaveDeletesAndReInsertsTranslations() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        Map<String, String> nameTranslations = new HashMap<>();
        nameTranslations.put("de", "Ubung");
        habit.setNameTranslations(nameTranslations);
        Map<String, String> descTranslations = new HashMap<>();
        descTranslations.put("de", "Tagliche Ubung");
        habit.setDescriptionTranslations(descTranslations);

        repository.save(habit);

        verify(db).delete(eq("habit_name_translations"), eq("habit_id = ?"), eq(new String[]{habit.getId()}));
        verify(db).delete(eq("habit_description_translations"), eq("habit_id = ?"), eq(new String[]{habit.getId()}));
        verify(db).insert(eq("habit_name_translations"), isNull(), any());
        verify(db).insert(eq("habit_description_translations"), isNull(), any());
    }

    @Test
    public void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM habits WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        Optional<Habit> result = repository.findById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByIdFound() {
        Cursor habitCursor = createHabitCursor("h1", "Exercise", "Daily exercise", "DAILY", null, null);
        when(db.rawQuery(eq("SELECT * FROM habits WHERE id = ?"), eq(new String[]{"h1"}))).thenReturn(habitCursor);

        Cursor entryCursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM habit_entries"), any())).thenReturn(entryCursor);

        Cursor nameTrans = createEmptyCursor();
        Cursor descTrans = createEmptyCursor();
        when(db.rawQuery(contains("habit_name_translations"), any())).thenReturn(nameTrans);
        when(db.rawQuery(contains("habit_description_translations"), any())).thenReturn(descTrans);

        Optional<Habit> result = repository.findById("h1");

        assertTrue(result.isPresent());
        assertEquals("Exercise", result.get().getName());
        assertEquals("Daily exercise", result.get().getDescription());
        assertEquals(FrequencyType.DAILY, result.get().getFrequencyType());
    }

    @Test
    public void testFindByIdWithUser() {
        Cursor habitCursor = createHabitCursor("h1", "Exercise", "Daily exercise", "DAILY", "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM habits WHERE id = ?"), eq(new String[]{"h1"}))).thenReturn(habitCursor);

        Cursor entryCursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM habit_entries"), any())).thenReturn(entryCursor);

        Cursor nameTrans = createEmptyCursor();
        Cursor descTrans = createEmptyCursor();
        when(db.rawQuery(contains("habit_name_translations"), any())).thenReturn(nameTrans);
        when(db.rawQuery(contains("habit_description_translations"), any())).thenReturn(descTrans);

        Optional<Habit> result = repository.findById("h1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
        assertEquals("testuser", result.get().getUser().getUsername());
    }

    @Test
    public void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM habits"), isNull())).thenReturn(cursor);

        List<Habit> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteByIdWithTransaction() {
        repository.deleteById("h1");

        verify(db).beginTransaction();
        verify(db).delete(eq("habit_name_translations"), eq("habit_id = ?"), eq(new String[]{"h1"}));
        verify(db).delete(eq("habit_description_translations"), eq("habit_id = ?"), eq(new String[]{"h1"}));
        verify(db).delete(eq("habit_entries"), eq("habit_id = ?"), eq(new String[]{"h1"}));
        verify(db).delete(eq("habits"), eq("id = ?"), eq(new String[]{"h1"}));
        verify(db).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    public void testExistsByIdTrue() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(db.rawQuery(eq("SELECT 1 FROM habits WHERE id = ?"), eq(new String[]{"h1"}))).thenReturn(cursor);

        assertTrue(repository.existsById("h1"));
    }

    @Test
    public void testExistsByIdFalse() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT 1 FROM habits WHERE id = ?"), eq(new String[]{"h1"}))).thenReturn(cursor);

        assertFalse(repository.existsById("h1"));
    }

    @Test
    public void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM habits WHERE user_id"), any())).thenReturn(cursor);

        List<Habit> result = repository.findByUserId("u1");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSaveReturnsHabit() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        Habit result = repository.save(habit);
        assertSame(habit, result);
    }

    private Cursor createHabitCursor(String id, String name, String description, String frequencyType, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("name")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("description")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("category_id")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("frequency_type")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("target_frequency")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("max_entries_per_day")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("positive_scoring")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("scoring_rule_id")).thenReturn(9);
        when(cursor.getColumnIndexOrThrow("scoring_rule_name")).thenReturn(10);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(11);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(12);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);
        when(cursor.isNull(7)).thenReturn(false);
        when(cursor.isNull(8)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(name);
        when(cursor.getString(2)).thenReturn(description);
        when(cursor.getString(4)).thenReturn(frequencyType);
        when(cursor.getInt(5)).thenReturn(1);
        when(cursor.getInt(6)).thenReturn(1);
        when(cursor.getInt(7)).thenReturn(1);
        when(cursor.getString(8)).thenReturn("2024-01-15T10:00:00");

        if (userId != null) {
            when(cursor.isNull(11)).thenReturn(false);
            when(cursor.isNull(12)).thenReturn(false);
            when(cursor.getString(11)).thenReturn(userId);
            when(cursor.getString(12)).thenReturn(userName);
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
