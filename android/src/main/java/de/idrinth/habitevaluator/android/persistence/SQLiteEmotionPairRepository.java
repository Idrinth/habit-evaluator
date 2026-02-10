package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEmotionPairRepository implements EmotionPairRepository {

    private final SQLiteHelper dbHelper;

    public SQLiteEmotionPairRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public EmotionPair save(EmotionPair pair) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", pair.getId());
        values.put("negative_label", pair.getNegativeLabel());
        values.put("positive_label", pair.getPositiveLabel());
        if (pair.getUser() != null) {
            values.put("user_id", pair.getUser().getId());
            values.put("user_name", pair.getUser().getUsername());
        }
        db.insertWithOnConflict("emotion_pairs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return pair;
    }

    @Override
    public Optional<EmotionPair> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emotion_pairs WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readFromCursor(cursor));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<EmotionPair> findAll() {
        List<EmotionPair> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emotion_pairs", null)) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("emotion_pairs", "id = ?", new String[]{id});
    }

    @Override
    public List<EmotionPair> findByUserId(String userId) {
        List<EmotionPair> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM emotion_pairs WHERE user_id = ? ORDER BY negative_label COLLATE NOCASE", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readFromCursor(cursor));
            }
        }
        return result;
    }

    private EmotionPair readFromCursor(Cursor cursor) {
        EmotionPair pair = new EmotionPair();
        pair.setId(getString(cursor, "id"));
        pair.setNegativeLabel(getString(cursor, "negative_label"));
        pair.setPositiveLabel(getString(cursor, "positive_label"));

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            pair.setUser(user);
        }

        return pair;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? null : cursor.getString(idx);
    }
}
