package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.SleepEntry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteSleepEntryRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteSleepEntryRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteSleepEntryRepository(dbHelper);
    }

    @Test
    void testSaveCallsInsert() {
        SleepEntry entry = new SleepEntry();
        entry.setFromTime(LocalTime.of(23, 0));
        entry.setUntilTime(LocalTime.of(7, 0));
        entry.setDate(LocalDate.of(2024, 6, 15));

        repository.save(entry);

        verify(db).insertWithOnConflict(eq("sleep_entries"), isNull(), any(), eq(5));
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM sleep_entries WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createSleepCursor("s1", "23:00", "07:00", "2024-06-15", "Good sleep", null, null);
        when(db.rawQuery(eq("SELECT * FROM sleep_entries WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<SleepEntry> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertEquals(LocalTime.of(23, 0), result.get().getFromTime());
        assertEquals(LocalTime.of(7, 0), result.get().getUntilTime());
        assertEquals(LocalDate.of(2024, 6, 15), result.get().getDate());
        assertEquals("Good sleep", result.get().getNotes());
    }

    @Test
    void testFindByIdWithUser() {
        Cursor cursor = createSleepCursor("s1", "23:00", "07:00", "2024-06-15", null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM sleep_entries WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<SleepEntry> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
        assertEquals("testuser", result.get().getUser().getUsername());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM sleep_entries"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("s1");

        verify(db).delete(eq("sleep_entries"), eq("id = ?"), eq(new String[]{"s1"}));
    }

    @Test
    void testExistsByIdTrue() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(db.rawQuery(eq("SELECT 1 FROM sleep_entries WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        assertTrue(repository.existsById("s1"));
    }

    @Test
    void testExistsByIdFalse() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT 1 FROM sleep_entries WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        assertFalse(repository.existsById("s1"));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM sleep_entries WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    void testSaveReturnsEntry() {
        SleepEntry entry = new SleepEntry();
        SleepEntry result = repository.save(entry);
        assertSame(entry, result);
    }

    private Cursor createSleepCursor(String id, String fromTime, String untilTime, String date, String notes, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("from_time")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("until_time")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("date")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(7);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(fromTime);
        when(cursor.getString(2)).thenReturn(untilTime);
        when(cursor.getString(3)).thenReturn(date);
        when(cursor.getString(4)).thenReturn("2024-06-15T10:00:00");

        if (notes != null) {
            when(cursor.isNull(5)).thenReturn(false);
            when(cursor.getString(5)).thenReturn(notes);
        }
        if (userId != null) {
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.isNull(7)).thenReturn(false);
            when(cursor.getString(6)).thenReturn(userId);
            when(cursor.getString(7)).thenReturn(userName);
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
