package de.idrinth.habitevaluator.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SQLiteEmergencyPlanActionRepositoryTest {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;
    private SQLiteEmergencyPlanActionRepository repository;

    @BeforeEach
    void setUp() {
        dbHelper = mock(SQLiteHelper.class);
        db = mock(SQLiteDatabase.class);
        when(dbHelper.getReadableDatabase()).thenReturn(db);
        when(dbHelper.getWritableDatabase()).thenReturn(db);
        repository = new SQLiteEmergencyPlanActionRepository(dbHelper);
    }

    @Test
    void testSaveCallsInsert() {
        EmergencyPlanAction action = new EmergencyPlanAction("Do something", 0);

        repository.save(action);

        verify(db).insertWithOnConflict(eq("emergency_plan_actions"), isNull(), any(), eq(5));
    }

    @Test
    void testSaveReturnsAction() {
        EmergencyPlanAction action = new EmergencyPlanAction("Do something", 0);
        EmergencyPlanAction result = repository.save(action);
        assertSame(action, result);
    }

    @Test
    void testSaveAllUsesTransaction() {
        EmergencyPlanAction a1 = new EmergencyPlanAction("Action 1", 0);
        EmergencyPlanAction a2 = new EmergencyPlanAction("Action 2", 1);

        repository.saveAll(Arrays.asList(a1, a2));

        verify(db).beginTransaction();
        verify(db, times(2)).insertWithOnConflict(eq("emergency_plan_actions"), isNull(), any(), eq(5));
        verify(db).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    void testSaveAllEndsTransactionOnFailure() {
        EmergencyPlanAction action = new EmergencyPlanAction("Action", 0);
        doThrow(new RuntimeException("DB error")).when(db)
                .insertWithOnConflict(eq("emergency_plan_actions"), isNull(), any(), eq(5));

        assertThrows(RuntimeException.class, () -> repository.saveAll(Arrays.asList(action)));

        verify(db).beginTransaction();
        verify(db, never()).setTransactionSuccessful();
        verify(db).endTransaction();
    }

    @Test
    void testFindByIdNotFound() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_actions WHERE id = ?"), eq(new String[]{"nonexistent"}))).thenReturn(cursor);

        assertFalse(repository.findById("nonexistent").isPresent());
    }

    @Test
    void testFindByIdFound() {
        Cursor cursor = createActionCursor("a1", "Take a walk", null, 0, null);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_actions WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        Optional<EmergencyPlanAction> result = repository.findById("a1");

        assertTrue(result.isPresent());
        assertEquals("Take a walk", result.get().getActionText());
        assertNull(result.get().getPhoneNumber());
        assertEquals(0, result.get().getActionOrder());
    }

    @Test
    void testFindByIdWithPhone() {
        Cursor cursor = createActionCursor("a1", "Call doctor", "+49123456", 1, "s1");
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_actions WHERE id = ?"), eq(new String[]{"a1"}))).thenReturn(cursor);

        Optional<EmergencyPlanAction> result = repository.findById("a1");

        assertTrue(result.isPresent());
        assertEquals("+49123456", result.get().getPhoneNumber());
        assertNotNull(result.get().getStep());
        assertEquals("s1", result.get().getStep().getId());
    }

    @Test
    void testFindByStepIdEmpty() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToNext()).thenReturn(false);
        when(db.rawQuery(eq("SELECT * FROM emergency_plan_actions WHERE step_id = ? ORDER BY action_order"), eq(new String[]{"s1"}))).thenReturn(cursor);

        assertTrue(repository.findByStepId("s1").isEmpty());
    }

    @Test
    void testDeleteById() {
        repository.deleteById("a1");
        verify(db).delete(eq("emergency_plan_actions"), eq("id = ?"), eq(new String[]{"a1"}));
    }

    @Test
    void testDeleteByStepId() {
        repository.deleteByStepId("s1");
        verify(db).delete(eq("emergency_plan_actions"), eq("step_id = ?"), eq(new String[]{"s1"}));
    }

    private Cursor createActionCursor(String id, String actionText, String phoneNumber, int actionOrder, String stepId) {
        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);

        when(cursor.getColumnIndexOrThrow("id")).thenReturn(0);
        when(cursor.getColumnIndexOrThrow("action_text")).thenReturn(1);
        when(cursor.getColumnIndexOrThrow("phone_number")).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("action_order")).thenReturn(3);
        when(cursor.getColumnIndexOrThrow("step_id")).thenReturn(4);

        when(cursor.isNull(anyInt())).thenReturn(true);
        when(cursor.isNull(0)).thenReturn(false);
        when(cursor.isNull(1)).thenReturn(false);
        when(cursor.isNull(3)).thenReturn(false);

        when(cursor.getString(0)).thenReturn(id);
        when(cursor.getString(1)).thenReturn(actionText);
        when(cursor.getInt(3)).thenReturn(actionOrder);

        if (phoneNumber != null) {
            when(cursor.isNull(2)).thenReturn(false);
            when(cursor.getString(2)).thenReturn(phoneNumber);
        }

        if (stepId != null) {
            when(cursor.isNull(4)).thenReturn(false);
            when(cursor.getString(4)).thenReturn(stepId);
        }

        return cursor;
    }
}
