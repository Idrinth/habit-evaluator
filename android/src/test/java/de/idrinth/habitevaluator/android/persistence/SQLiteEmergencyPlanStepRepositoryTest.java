package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteEmergencyPlanStepRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteEmergencyPlanStepRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteEmergencyPlanStepRepository(dbHelper);
    }

    @Test
    void testSaveCallsInsert() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", "Action", 0);

        repository.save(step);

        verify(db).insertWithOnConflict(eq("emergency_plan_steps"), isNull(), any(), eq(5));
    }

    @Test
    void testSaveReturnsStep() {
        EmergencyPlanStep step = new EmergencyPlanStep("Question?", "Action", 0);
        EmergencyPlanStep result = repository.save(step);
        assertSame(step, result);
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createStepCursor("s1", "Are you tired?", "Take a nap", null, 0, null, null);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<EmergencyPlanStep> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertEquals("Are you tired?", result.get().getQuestion());
        assertEquals("Take a nap", result.get().getAction());
        assertNull(result.get().getPhoneNumber());
        assertEquals(0, result.get().getStepOrder());
    }

    @Test
    void testFindByIdWithPhoneNumber() {
        Cursor cursor = createStepCursor("s1", "Need help?", "Call support", "+491234567890", 1, null, null);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<EmergencyPlanStep> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertEquals("+491234567890", result.get().getPhoneNumber());
    }

    @Test
    void testFindByIdWithUser() {
        Cursor cursor = createStepCursor("s1", "Q?", "A", null, 0, "u1", "testuser");
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps WHERE id = ?"), eq(new String[]{"s1"}))).thenReturn(cursor);

        Optional<EmergencyPlanStep> result = repository.findById("s1");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getUser());
        assertEquals("u1", result.get().getUser().getId());
    }

    @Test
    void testFindAllEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_steps ORDER BY step_order"), isNull())).thenReturn(cursor);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("s1");

        verify(db).delete(eq("emergency_plan_steps"), eq("id = ?"), eq(new String[]{"s1"}));
    }

    @Test
    void testFindByUserIdEmpty() {
        Cursor cursor = createEmptyCursor();
        when(db.rawQuery(startsWith("SELECT * FROM emergency_plan_steps WHERE user_id"), any())).thenReturn(cursor);

        assertTrue(repository.findByUserId("u1").isEmpty());
    }

    private Cursor createStepCursor(String id, String question, String action, String phoneNumber, int stepOrder, String userId, String userName) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("question")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("action")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("phone_number")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("step_order")).thenReturn(4);
        when(cursor.getColumnIndexOrThrow("user_id")).thenReturn(5);
        when(cursor.getColumnIndexOrThrow("user_name")).thenReturn(6);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(2)).thenReturn(false);
        when(cursor.isNull(4)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(question);
        when(cursor.getString(2)).thenReturn(action);
        when(cursor.getInt(4)).thenReturn(stepOrder);

        if (phoneNumber != null) {
            when(cursor.isNull(3)).thenReturn(false);
            when(cursor.getString(3)).thenReturn(phoneNumber);
        }
        if (userId != null) {
            when(cursor.isNull(5)).thenReturn(false);
            when(cursor.isNull(6)).thenReturn(false);
            when(cursor.getString(5)).thenReturn(userId);
            when(cursor.getString(6)).thenReturn(userName);
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
