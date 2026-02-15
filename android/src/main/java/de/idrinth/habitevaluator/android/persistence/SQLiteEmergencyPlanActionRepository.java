package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction;
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep;
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEmergencyPlanActionRepository implements EmergencyPlanActionRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteEmergencyPlanActionRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public EmergencyPlanAction save(EmergencyPlanAction action) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(action);
        db.insertWithOnConflict("emergency_plan_actions", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return action;
    }

    @Override
    public void saveAll(List<EmergencyPlanAction> actions) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (EmergencyPlanAction action : actions) {
                ContentValues values = toContentValues(action);
                db.insertWithOnConflict("emergency_plan_actions", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public Optional<EmergencyPlanAction> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emergency_plan_actions WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<EmergencyPlanAction> findByStepId(String stepId) {
        List<EmergencyPlanAction> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emergency_plan_actions WHERE step_id = ? ORDER BY action_order", new String[]{stepId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("emergency_plan_actions", "id = ?", new String[]{id});
    }

    @Override
    public void deleteByStepId(String stepId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("emergency_plan_actions", "step_id = ?", new String[]{stepId});
    }

    private ContentValues toContentValues(EmergencyPlanAction action) {
        ContentValues values = new ContentValues();
        values.put("id", action.getId());
        values.put("action_text", action.getActionText());
        values.put("phone_number", action.getPhoneNumber());
        values.put("action_order", action.getActionOrder());
        if (action.getStep() != null) {
            values.put("step_id", action.getStep().getId());
        }
        return values;
    }

    private EmergencyPlanAction readFromCursor(Cursor cursor) {
        EmergencyPlanAction action = new EmergencyPlanAction();
        action.setId(getString(cursor, "id"));
        action.setActionText(getString(cursor, "action_text"));
        action.setPhoneNumber(getString(cursor, "phone_number"));

        int orderIdx = cursor.getColumnIndexOrThrow("action_order");
        action.setActionOrder(cursor.getInt(orderIdx));

        String stepId = getString(cursor, "step_id");
        if (stepId != null) {
            EmergencyPlanStep step = new EmergencyPlanStep();
            step.setId(stepId);
            action.setStep(step);
        }

        return action;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
