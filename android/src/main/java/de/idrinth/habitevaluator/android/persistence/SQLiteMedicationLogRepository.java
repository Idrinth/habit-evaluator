package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.Medication;
import de.idrinth.habitevaluator.shared.model.MedicationLog;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteMedicationLogRepository implements MedicationLogRepository {

    private final SQLiteHelper dbHelper;
    private final SQLiteMedicationRepository medicationRepository;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteMedicationLogRepository(SQLiteHelper dbHelper, SQLiteMedicationRepository medicationRepository) {
        this.dbHelper = dbHelper;
        this.medicationRepository = medicationRepository;
    }

    @Override
    public MedicationLog save(MedicationLog entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", entry.getId());
        values.put("medication_id", entry.getMedication() != null ? entry.getMedication().getId() : null);
        values.put("amount", entry.getAmount());
        values.put("taken_at", entry.getTakenAt() != null ? entry.getTakenAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        values.put("notes", entry.getNotes());
        values.put("created_at", entry.getCreatedAt() != null ? entry.getCreatedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        if (entry.getUser() != null) {
            values.put("user_id", entry.getUser().getId());
            values.put("user_name", entry.getUser().getUsername());
        }
        db.insertWithOnConflict("medication_logs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return entry;
    }

    @Override
    public Optional<MedicationLog> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM medication_logs WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<MedicationLog> findAll() {
        List<MedicationLog> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM medication_logs", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("medication_logs", "id = ?", new String[]{id});
    }

    @Override
    public List<MedicationLog> findByUserId(String userId) {
        List<MedicationLog> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM medication_logs WHERE user_id = ?", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private MedicationLog readFromCursor(Cursor cursor) {
        MedicationLog entry = new MedicationLog();
        entry.setId(getString(cursor, "id"));

        String medicationId = getString(cursor, "medication_id");
        if (medicationId != null) {
            Optional<Medication> medication = medicationRepository.findById(medicationId);
            medication.ifPresent(entry::setMedication);
        }

        int amountIdx = cursor.getColumnIndexOrThrow("amount");
        if (!cursor.isNull(amountIdx)) {
            entry.setAmount(cursor.getDouble(amountIdx));
        }

        String takenAt = getString(cursor, "taken_at");
        if (takenAt != null) {
            entry.setTakenAt(LocalDateTime.parse(takenAt, DT_FORMAT));
        }

        entry.setNotes(getString(cursor, "notes"));

        String createdAt = getString(cursor, "created_at");
        if (createdAt != null) {
            entry.setCreatedAt(LocalDateTime.parse(createdAt, DT_FORMAT));
        }

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            entry.setUser(user);
        }

        return entry;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
