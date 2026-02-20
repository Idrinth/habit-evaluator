package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.ActivityLog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteActivityLogRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteActivityLogRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteActivityLogRepository(dbHelper);
    }

    @Test
    void testSaveCallsInsert() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office", LocalTime.of(9, 0), LocalTime.of(10, 0));

        repository.save(entry);

        verify(db).insertWithOnConflict(eq("activity_logs"), isNull(), any(), eq(5));
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM activity_logs WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createActivityLogCursor("a1", "Alice, Bob", "Office", "09:00", "10:00", "2024-06-15", "Team meeting", null, null);
        when(db.rawQuery(eq("SELECT * FROM activity_logs WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        Optional<ActivityLog> result = repository.findById("a1");

        assertTrue(result.isPresent());
        assertEquals("Alice, Bob", result.get().getPersons());
        assertEquals("Office", result.get().getLocation());
        assertEquals(LocalTime.of(9, 0), result.get().getStartTime());
        assertEquals(LocalTime.of(10, 0), result.get().getEndTime());
        assertEquals(LocalDate.of(2024, 6, 15), result.get().getDate());
        assertEquals("Team meeting", result.get().getActivity());
    }

    @Test
    void testFindByIdWithUser() {
        Cursor cursor = createActivityLogCursor("a1", "Alice, Bob", "Office", "09:00", "10:00", "2024-06-15", null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM activity_logs WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        Optional<ActivityLog> result = repository.findById("a1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM activity_logs"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("a1");

        verify(db).delete(eq("activity_logs"), eq("id = ?"), eq(new String[]{"a1"}));
    }

    @Test
    void testExistsByIdTrue() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(db.rawQuery(eq("SELECT 1 FROM activity_logs WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        assertTrue(repository.existsById("a1"));
    }

    @Test
    void testExistsByIdFalse() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT 1 FROM activity_logs WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        assertFalse(repository.existsById("a1"));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM activity_logs WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    void testSaveReturnsEntry() {
        ActivityLog entry = new ActivityLog("Alice, Bob", "Office", LocalTime.of(9, 0), LocalTime.of(10, 0));
        ActivityLog result = repository.save(entry);
        assertSame(entry, result);
    }

    @Test
    void testFindAllWithMultipleEntries() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, true, false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("persons")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("location")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("start_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("end_time")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("date")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("activity")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(9);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);
        when(cursor.isNull(7)).thenReturn(false);

        when(cursor.getString(0)).thenReturn("a1", "a2");
        when(cursor.getString(1)).thenReturn("Alice", "Bob");
        when(cursor.getString(2)).thenReturn("Office", "Park");
        when(cursor.getString(5)).thenReturn("2024-06-15");
        when(cursor.getString(7)).thenReturn("2024-06-15T10:00:00");

        when(db.rawQuery(eq("SELECT * FROM activity_logs"), isNull())).thenReturn(cursor);

        List<ActivityLog> result = repository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindByUserIdWithResults() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("persons")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("location")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("start_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("end_time")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("date")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("activity")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(9);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);
        when(cursor.isNull(7)).thenReturn(false);
        when(cursor.isNull(8)).thenReturn(false);
        when(cursor.isNull(9)).thenReturn(false);

        when(cursor.getString(0)).thenReturn("a1");
        when(cursor.getString(1)).thenReturn("Alice, Bob");
        when(cursor.getString(2)).thenReturn("Office");
        when(cursor.getString(5)).thenReturn("2024-06-15");
        when(cursor.getString(7)).thenReturn("2024-06-15T10:00:00");
        when(cursor.getString(8)).thenReturn("u1");
        when(cursor.getString(9)).thenReturn("testuser");

        when(db.rawQuery(startsWith("SELECT * FROM activity_logs WHERE user_id"), any())).thenReturn(cursor);

        List<ActivityLog> result = repository.findByUserId("u1");
        assertEquals(1, result.size());
        assertEquals("Alice, Bob", result.get(0).getPersons());
        assertNotNull(result.get(0).getUser());
    }

    @Test
    void testFindByIdWithNullTimesAndActivity() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("persons")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("location")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("start_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("end_time")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("date")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("activity")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(9);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);

        when(cursor.getString(0)).thenReturn("a1");
        when(cursor.getString(1)).thenReturn("Alice");
        when(cursor.getString(2)).thenReturn("Office");

        when(db.rawQuery(eq("SELECT * FROM activity_logs WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        Optional<ActivityLog> result = repository.findById("a1");
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getPersons());
        assertEquals("Office", result.get().getLocation());
        assertNull(result.get().getStartTime());
        assertNull(result.get().getEndTime());
        assertNull(result.get().getActivity());
        assertNull(result.get().getUser());
    }

    @Test
    void testSaveWithNullTimesDoesNotThrow() {
        ActivityLog entry = new ActivityLog();
        entry.setPersons("Alice");
        entry.setLocation("Office");
        entry.setStartTime(null);
        entry.setEndTime(null);

        repository.save(entry);
        verify(db).insertWithOnConflict(eq("activity_logs"), isNull(), any(), eq(5));
    }

    @Test
    void testFindDistinctLocationsByUserId() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getString(0)).thenReturn("Cafe", "Office");
        when(db.rawQuery(startsWith("SELECT DISTINCT location FROM activity_logs"), eq(new String[]{"u1"}))).thenReturn(cursor);

        List<String> result = repository.findDistinctLocationsByUserId("u1");

        assertEquals(2, result.size());
        assertEquals("Cafe", result.get(0));
        assertEquals("Office", result.get(1));
    }

    @Test
    void testFindDistinctLocationsByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT DISTINCT location FROM activity_logs"), eq(new String[]{"u1"}))).thenReturn(cursor);

        List<String> result = repository.findDistinctLocationsByUserId("u1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindDistinctActivitiesByUserId() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getString(0)).thenReturn("Lunch", "Team meeting");
        when(db.rawQuery(startsWith("SELECT DISTINCT activity FROM activity_logs"), eq(new String[]{"u1"}))).thenReturn(cursor);

        List<String> result = repository.findDistinctActivitiesByUserId("u1");

        assertEquals(2, result.size());
        assertEquals("Lunch", result.get(0));
        assertEquals("Team meeting", result.get(1));
    }

    @Test
    void testFindDistinctActivitiesByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT DISTINCT activity FROM activity_logs"), eq(new String[]{"u1"}))).thenReturn(cursor);

        List<String> result = repository.findDistinctActivitiesByUserId("u1");
        assertTrue(result.isEmpty());
    }

    private Cursor createActivityLogCursor(String id, String persons, String location, String startTime, String endTime, String date, String activity, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("persons")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("location")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("start_time")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("end_time")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("date")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("activity")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(9);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);
        when(cursor.isNull(7)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(persons);
        when(cursor.getString(2)).thenReturn(location);
        when(cursor.getString(3)).thenReturn(startTime);
        when(cursor.getString(4)).thenReturn(endTime);
        when(cursor.getString(5)).thenReturn(date);
        when(cursor.getString(7)).thenReturn("2024-06-15T10:00:00");

        if (activity != null) {
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.getString(6)).thenReturn(activity);
        }
        if (userId != null) {
            when(cursor.isNull(8)).thenReturn(false);
            when(cursor.isNull(9)).thenReturn(false);
            when(cursor.getString(8)).thenReturn(userId);
            when(cursor.getString(9)).thenReturn(userName);
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
