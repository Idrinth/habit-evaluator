package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEmotionEntryRepository implements EmotionEntryRepository {

    private final SQLiteHelper dbHelper;
    private final EmotionPairRepository emotionPairRepository;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteEmotionEntryRepository(SQLiteHelper dbHelper, EmotionPairRepository emotionPairRepository) {
        this.dbHelper = dbHelper;
        this.emotionPairRepository = emotionPairRepository;
    }

    @Override
    public EmotionEntry save(EmotionEntry entry) {
        if (entry.getEmotionPair() == null) {
            throw new IllegalArgumentException("EmotionEntry requires a non-null EmotionPair");
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", entry.getId());
        values.put("emotion_pair_id", entry.getEmotionPair().getId());
        values.put("strength", entry.getStrength());
        values.put("recorded_at", entry.getRecordedAt() != null ? entry.getRecordedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
        values.put("notes", entry.getNotes());
        if (entry.getUser() != null) {
            values.put("user_id", entry.getUser().getId());
            values.put("user_name", entry.getUser().getUsername());
        }
        db.insertWithOnConflict("emotion_entries", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return entry;
    }

    @Override
    public Optional<EmotionEntry> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emotion_entries WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                EmotionEntry entry = readFromCursor(cursor);
                return entry != null ? Optional.of(entry) : Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public List<EmotionEntry> findAll() {
        List<EmotionEntry> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emotion_entries", null)) {
            while (cursor.moveToNext()) {
                EmotionEntry entry = readFromCursor(cursor);
                if (entry != null) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("emotion_entries", "id = ?", new String[]{id});
    }

    @Override
    public List<EmotionEntry> findByUserId(String userId) {
        List<EmotionEntry> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emotion_entries WHERE user_id = ? ORDER BY recorded_at DESC", new String[]{userId})) {
            while (cursor.moveToNext()) {
                EmotionEntry entry = readFromCursor(cursor);
                if (entry != null) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    private EmotionEntry readFromCursor(Cursor cursor) {
        EmotionEntry entry = new EmotionEntry();
        entry.setId(getString(cursor, "id"));
        entry.setStrength(getInt(cursor, "strength"));

        String recordedAt = getString(cursor, "recorded_at");
        if (recordedAt != null) {
            entry.setRecordedAt(LocalDateTime.parse(recordedAt, DT_FORMAT));
        }

        entry.setNotes(getString(cursor, "notes"));

        String pairId = getString(cursor, "emotion_pair_id");
        if (pairId != null) {
            Optional<EmotionPair> pair = emotionPairRepository.findById(pairId);
            if (pair.isPresent()) {
                entry.setEmotionPair(pair.get());
            } else {
                return null;
            }
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

    private int getInt(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? 0 : cursor.getInt(idx);
    }
}
