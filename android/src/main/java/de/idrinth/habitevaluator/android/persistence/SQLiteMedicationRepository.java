package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteMedicationRepository implements MedicationRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteMedicationRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Medication save(Medication medication) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", medication.getId());
        values.put("name", medication.getName());
        values.put("wikipedia_link", medication.getWikipediaLink());
        values.put("provision_type", medication.getProvisionType() != null ? medication.getProvisionType().name() : null);
        if (medication.getUser() != null) {
            values.put("user_id", medication.getUser().getId());
            values.put("user_name", medication.getUser().getUsername());
        }
        db.insertWithOnConflict("medications", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return medication;
    }

    @Override
    public Optional<Medication> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM medications WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Medication> findAll() {
        List<Medication> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM medications", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("medications", "id = ?", new String[]{id});
    }

    @Override
    public List<Medication> findByUserId(String userId) {
        List<Medication> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM medications WHERE user_id = ? ORDER BY name COLLATE NOCASE", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private Medication readFromCursor(Cursor cursor) {
        Medication medication = new Medication();
        medication.setId(getString(cursor, "id"));
        medication.setName(getString(cursor, "name"));
        medication.setWikipediaLink(getString(cursor, "wikipedia_link"));

        String provisionType = getString(cursor, "provision_type");
        if (provisionType != null) {
            medication.setProvisionType(MedicationProvisionType.valueOf(provisionType));
        }

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            medication.setUser(user);
        }

        return medication;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
