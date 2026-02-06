package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EventSignificance;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteDiaryEntryRepository implements DiaryEntryRepository {

    private final SQLiteHelper dbHelper;
    private DiaryReferenceRepository diaryReferenceRepository;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public SQLiteDiaryEntryRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setDiaryReferenceRepository(DiaryReferenceRepository diaryReferenceRepository) {
        this.diaryReferenceRepository = diaryReferenceRepository;
    }

    @Override
    public DiaryEntry save(DiaryEntry entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", entry.getId());
        values.put("legacy_description", entry.getLegacyDescription());
        if (entry.getDiaryReference() != null) {
            values.put("diary_reference_id", entry.getDiaryReference().getId());
        } else {
            values.putNull("diary_reference_id");
        }
        values.put("significance", entry.getSignificance() != null ? entry.getSignificance().name() : EventSignificance.NORMAL.name());
        values.put("event_date", entry.getEventDate() != null ? entry.getEventDate().format(DATE_FORMAT) : LocalDate.now().format(DATE_FORMAT));
        values.put("created_at", entry.getCreatedAt() != null ? entry.getCreatedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        if (entry.getUser() != null) {
            values.put("user_id", entry.getUser().getId());
            values.put("user_name", entry.getUser().getUsername());
        }
        db.insertWithOnConflict("diary_entries", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return entry;
    }

    @Override
    public Optional<DiaryEntry> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM diary_entries WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DiaryEntry> findAll() {
        List<DiaryEntry> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM diary_entries", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("diary_entries", "id = ?", new String[]{id});
    }

    @Override
    public List<DiaryEntry> findByUserId(String userId) {
        List<DiaryEntry> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM diary_entries WHERE user_id = ?", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public List<String> findDistinctDescriptionsByUserId(String userId) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Combine descriptions from references and legacy descriptions
        try (Cursor cursor = db.rawQuery(
                "SELECT DISTINCT dr.description FROM diary_entries de "
                        + "JOIN diary_references dr ON de.diary_reference_id = dr.id "
                        + "WHERE de.user_id = ? AND dr.description IS NOT NULL AND dr.description != '' "
                        + "UNION "
                        + "SELECT DISTINCT de.legacy_description FROM diary_entries de "
                        + "WHERE de.user_id = ? AND de.diary_reference_id IS NULL AND de.legacy_description IS NOT NULL AND de.legacy_description != '' "
                        + "ORDER BY 1",
                new String[]{userId, userId})) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        }
        return result;
    }

    @Override
    public List<DiaryEntry> findEntriesNeedingMigration(String userId) {
        List<DiaryEntry> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM diary_entries WHERE user_id = ? AND diary_reference_id IS NULL AND legacy_description IS NOT NULL AND legacy_description != ''",
                new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private DiaryEntry readFromCursor(Cursor cursor) {
        DiaryEntry entry = new DiaryEntry();
        entry.setId(getString(cursor, "id"));
        entry.setLegacyDescription(getString(cursor, "legacy_description"));

        String refId = getString(cursor, "diary_reference_id");
        if (refId != null && diaryReferenceRepository != null) {
            diaryReferenceRepository.findById(refId).ifPresent(entry::setDiaryReference);
        }

        String significance = getString(cursor, "significance");
        if (significance != null) {
            entry.setSignificance(EventSignificance.valueOf(significance));
        }

        String eventDate = getString(cursor, "event_date");
        if (eventDate != null) {
            entry.setEventDate(LocalDate.parse(eventDate, DATE_FORMAT));
        }

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
