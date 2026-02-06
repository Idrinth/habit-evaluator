package de.idrinth.habitevaluator.android.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLiteHabitRepository implements HabitRepository {

    private final SQLiteHelper dbHelper;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteHabitRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Habit save(Habit habit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("id", habit.getId());
            values.put("name", habit.getName());
            values.put("description", habit.getDescription());
            values.put("category_id", habit.getCategoryId());
            values.put("frequency_type", habit.getFrequencyType() != null ? habit.getFrequencyType().name() : FrequencyType.DAILY.name());
            values.put("target_frequency", habit.getTargetFrequency());
            values.put("max_entries_per_day", habit.getMaxEntriesPerDay());
            values.put("positive_scoring", habit.isPositiveScoring() ? 1 : 0);
            values.put("created_at", habit.getCreatedAt() != null ? habit.getCreatedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
            if (habit.getScoringRule() != null) {
                values.put("scoring_rule_id", habit.getScoringRule().getId());
                values.put("scoring_rule_name", habit.getScoringRule().getName());
            } else {
                values.putNull("scoring_rule_id");
                values.putNull("scoring_rule_name");
            }
            if (habit.getUser() != null) {
                values.put("user_id", habit.getUser().getId());
                values.put("user_name", habit.getUser().getUsername());
            }
            db.insertWithOnConflict("habits", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            // Delete and re-insert entries
            db.delete("habit_entries", "habit_id = ?", new String[]{habit.getId()});
            if (habit.getEntries() != null) {
                for (HabitEntry entry : habit.getEntries()) {
                    ContentValues ev = new ContentValues();
                    ev.put("id", entry.getId());
                    ev.put("habit_id", habit.getId());
                    ev.put("completed_at", entry.getCompletedAt() != null ? entry.getCompletedAt().format(DT_FORMAT) : LocalDateTime.now().format(DT_FORMAT));
                    ev.put("notes", entry.getNotes());
                    ev.put("value", entry.getValue());
                    db.insertWithOnConflict("habit_entries", null, ev, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }

            // Delete and re-insert translations
            db.delete("habit_name_translations", "habit_id = ?", new String[]{habit.getId()});
            if (habit.getNameTranslations() != null) {
                for (Map.Entry<String, String> e : habit.getNameTranslations().entrySet()) {
                    ContentValues tv = new ContentValues();
                    tv.put("habit_id", habit.getId());
                    tv.put("language", e.getKey());
                    tv.put("translated_name", e.getValue());
                    db.insert("habit_name_translations", null, tv);
                }
            }
            db.delete("habit_description_translations", "habit_id = ?", new String[]{habit.getId()});
            if (habit.getDescriptionTranslations() != null) {
                for (Map.Entry<String, String> e : habit.getDescriptionTranslations().entrySet()) {
                    ContentValues tv = new ContentValues();
                    tv.put("habit_id", habit.getId());
                    tv.put("language", e.getKey());
                    tv.put("translated_description", e.getValue());
                    db.insert("habit_description_translations", null, tv);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return habit;
    }

    @Override
    public Optional<Habit> findById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM habits WHERE id = ?", new String[]{id})) {
            if (cursor.moveToFirst()) {
                return Optional.of(readHabitFromCursor(cursor, db));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Habit> findAll() {
        List<Habit> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM habits", null)) {
            while (cursor.moveToNext()) {
                result.add(readHabitFromCursor(cursor, db));
            }
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("habit_name_translations", "habit_id = ?", new String[]{id});
            db.delete("habit_description_translations", "habit_id = ?", new String[]{id});
            db.delete("habit_entries", "habit_id = ?", new String[]{id});
            db.delete("habits", "id = ?", new String[]{id});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean existsById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM habits WHERE id = ?", new String[]{id})) {
            return cursor.moveToFirst();
        }
    }

    @Override
    public List<Habit> findByUserId(String userId) {
        List<Habit> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM habits WHERE user_id = ?", new String[]{userId})) {
            while (cursor.moveToNext()) {
                result.add(readHabitFromCursor(cursor, db));
            }
        }
        return result;
    }

    private Habit readHabitFromCursor(Cursor cursor, SQLiteDatabase db) {
        Habit habit = new Habit();
        habit.setId(getString(cursor, "id"));
        habit.setName(getString(cursor, "name"));
        habit.setDescription(getString(cursor, "description"));
        habit.setCategoryId(getString(cursor, "category_id"));

        String freqType = getString(cursor, "frequency_type");
        if (freqType != null) {
            habit.setFrequencyType(FrequencyType.valueOf(freqType));
        }

        habit.setTargetFrequency(getInt(cursor, "target_frequency"));
        habit.setMaxEntriesPerDay(getInt(cursor, "max_entries_per_day"));
        habit.setPositiveScoring(getInt(cursor, "positive_scoring") == 1);

        String createdAt = getString(cursor, "created_at");
        if (createdAt != null) {
            habit.setCreatedAt(LocalDateTime.parse(createdAt, DT_FORMAT));
        }

        String ruleId = getString(cursor, "scoring_rule_id");
        if (ruleId != null) {
            ScoringRule rule = new ScoringRule();
            rule.setId(ruleId);
            rule.setName(getString(cursor, "scoring_rule_name"));
            habit.setScoringRule(rule);
        }

        String userId = getString(cursor, "user_id");
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(getString(cursor, "user_name"));
            user.setPassword("placeholder");
            habit.setUser(user);
        }

        // Load entries
        List<HabitEntry> entries = new ArrayList<>();
        try (Cursor ec = db.rawQuery("SELECT * FROM habit_entries WHERE habit_id = ?", new String[]{habit.getId()})) {
            while (ec.moveToNext()) {
                HabitEntry entry = new HabitEntry();
                entry.setId(getString(ec, "id"));
                String completedAt = getString(ec, "completed_at");
                if (completedAt != null) {
                    entry.setCompletedAt(LocalDateTime.parse(completedAt, DT_FORMAT));
                }
                entry.setNotes(getString(ec, "notes"));
                entry.setValue(getInt(ec, "value"));
                entry.setHabit(habit);
                entries.add(entry);
            }
        }
        habit.setEntries(entries);

        // Load translations
        habit.setNameTranslations(loadTranslations(db, "habit_name_translations", "habit_id", habit.getId(), "translated_name"));
        habit.setDescriptionTranslations(loadTranslations(db, "habit_description_translations", "habit_id", habit.getId(), "translated_description"));

        return habit;
    }

    private Map<String, String> loadTranslations(SQLiteDatabase db, String table, String fkColumn, String fkValue, String valueColumn) {
        Map<String, String> translations = new HashMap<>();
        try (Cursor cursor = db.rawQuery("SELECT language, " + valueColumn + " FROM " + table + " WHERE " + fkColumn + " = ?", new String[]{fkValue})) {
            while (cursor.moveToNext()) {
                String lang = cursor.getString(cursor.getColumnIndexOrThrow("language"));
                String val = cursor.getString(cursor.getColumnIndexOrThrow(valueColumn));
                if (lang != null && val != null) {
                    translations.put(lang, val);
                }
            }
        }
        return translations;
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
