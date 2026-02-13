package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.SportLog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteSportLogRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteSportLogRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteSportLogRepository(dbHelper);
    }

    @Test
    void testSaveCallsInsert() {
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));

        repository.save(entry);

        verify(db).insertWithOnConflict(eq("sport_logs"), isNull(), any(), eq(5));
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM sport_logs WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createSportLogCursor("s1", "Running", 5.5, "km", "08:00", "09:00", "2024-06-15", "Morning run", null, null);
        when(db.rawQuery(eq("SELECT * FROM sport_logs WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<SportLog> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertEquals("Running", result.get().getName());
        assertEquals(5.5, result.get().getMeasurement(), 0.01);
        assertEquals("km", result.get().getMeasurementUnit());
        assertEquals(LocalTime.of(8, 0), result.get().getStartTime());
        assertEquals(LocalTime.of(9, 0), result.get().getEndTime());
        assertEquals(LocalDate.of(2024, 6, 15), result.get().getDate());
        assertEquals("Morning run", result.get().getNotes());
    }

    @Test
    void testFindByIdWithUser() {
        Cursor cursor = createSportLogCursor("s1", "Running", 5.0, "km", "08:00", "09:00", "2024-06-15", null, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM sport_logs WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<SportLog> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM sport_logs"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("s1");

        verify(db).delete(eq("sport_logs"), eq("id = ?"), eq(new String[]{"s1"}));
    }

    @Test
    void testExistsByIdTrue() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(db.rawQuery(eq("SELECT 1 FROM sport_logs WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        assertTrue(repository.existsById("s1"));
    }

    @Test
    void testExistsByIdFalse() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT 1 FROM sport_logs WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        assertFalse(repository.existsById("s1"));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM sport_logs WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    @Test
    void testSaveReturnsEntry() {
        SportLog entry = new SportLog("Running", 5.0, "km", LocalTime.of(8, 0), LocalTime.of(9, 0));
        SportLog result = repository.save(entry);
        assertSame(entry, result);
    }

    private Cursor createSportLogCursor(String id, String name, double measurement, String measurementUnit, String startTime, String endTime, String date, String notes, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("name")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("measurement")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("measurement_unit")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("start_time")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("end_time")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("date")).thenReturn(6);
        when(cursor.getColumnIndexOrThrow("created_at")).thenReturn(7);
        when(cursor.getColumnIndexOrThrow("notes")).thenReturn(8);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(9);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(10);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);
        when(cursor.isNull(5)).thenReturn(false);
        when(cursor.isNull(6)).thenReturn(false);
        when(cursor.isNull(7)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(name);
        when(cursor.getDouble(2)).thenReturn(measurement);
        when(cursor.getString(3)).thenReturn(measurementUnit);
        when(cursor.getString(4)).thenReturn(startTime);
        when(cursor.getString(5)).thenReturn(endTime);
        when(cursor.getString(6)).thenReturn(date);
        when(cursor.getString(7)).thenReturn("2024-06-15T10:00:00");

        if (notes != null) {
            when(cursor.isNull(8)).thenReturn(false);
            when(cursor.getString(8)).thenReturn(notes);
        }
        if (userId != null) {
            when(cursor.isNull(9)).thenReturn(false);
            when(cursor.isNull(10)).thenReturn(false);
            when(cursor.getString(9)).thenReturn(userId);
            when(cursor.getString(10)).thenReturn(userName);
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
