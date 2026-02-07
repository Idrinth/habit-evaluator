package de.idrinth.habitevaluator.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habit_evaluator.db";
    private static final int DATABASE_VERSION = 4;
    private static SQLiteHelper instance;

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE habits ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "description TEXT,"
                + "category_id TEXT,"
                + "frequency_type TEXT NOT NULL,"
                + "target_frequency INTEGER NOT NULL,"
                + "max_entries_per_day INTEGER NOT NULL,"
                + "positive_scoring INTEGER NOT NULL DEFAULT 1,"
                + "created_at TEXT NOT NULL,"
                + "scoring_rule_id TEXT,"
                + "scoring_rule_name TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE habit_entries ("
                + "id TEXT PRIMARY KEY,"
                + "habit_id TEXT NOT NULL,"
                + "completed_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "value INTEGER NOT NULL DEFAULT 1,"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE habit_name_translations ("
                + "habit_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_name TEXT NOT NULL,"
                + "PRIMARY KEY (habit_id, language),"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE habit_description_translations ("
                + "habit_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_description TEXT NOT NULL,"
                + "PRIMARY KEY (habit_id, language),"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE habit_categories ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT,"
                + "description TEXT,"
                + "color TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE category_name_translations ("
                + "category_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_name TEXT NOT NULL,"
                + "PRIMARY KEY (category_id, language),"
                + "FOREIGN KEY (category_id) REFERENCES habit_categories(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE category_description_translations ("
                + "category_id TEXT NOT NULL,"
                + "language TEXT NOT NULL,"
                + "translated_description TEXT NOT NULL,"
                + "PRIMARY KEY (category_id, language),"
                + "FOREIGN KEY (category_id) REFERENCES habit_categories(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE diary_references ("
                + "id TEXT PRIMARY KEY,"
                + "description TEXT NOT NULL,"
                + "description_lower TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE diary_entries ("
                + "id TEXT PRIMARY KEY,"
                + "legacy_description TEXT,"
                + "diary_reference_id TEXT,"
                + "significance TEXT NOT NULL,"
                + "event_date TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT,"
                + "start_time TEXT,"
                + "end_time TEXT,"
                + "FOREIGN KEY (diary_reference_id) REFERENCES diary_references(id)"
                + ")");

        db.execSQL("CREATE TABLE sleep_entries ("
                + "id TEXT PRIMARY KEY,"
                + "from_time TEXT NOT NULL,"
                + "until_time TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE emotion_pairs ("
                + "id TEXT PRIMARY KEY,"
                + "negative_label TEXT NOT NULL,"
                + "positive_label TEXT NOT NULL,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE TABLE emotion_entries ("
                + "id TEXT PRIMARY KEY,"
                + "emotion_pair_id TEXT NOT NULL,"
                + "strength INTEGER NOT NULL,"
                + "recorded_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT,"
                + "FOREIGN KEY (emotion_pair_id) REFERENCES emotion_pairs(id)"
                + ")");

        db.execSQL("CREATE TABLE sport_logs ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "measurement REAL NOT NULL,"
                + "measurement_unit TEXT NOT NULL,"
                + "start_time TEXT NOT NULL,"
                + "end_time TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");

        db.execSQL("CREATE INDEX idx_habit_entries_habit_id ON habit_entries(habit_id)");
        db.execSQL("CREATE INDEX idx_habits_user_id ON habits(user_id)");
        db.execSQL("CREATE INDEX idx_habit_categories_user_id ON habit_categories(user_id)");
        db.execSQL("CREATE INDEX idx_diary_entries_user_id ON diary_entries(user_id)");
        db.execSQL("CREATE INDEX idx_diary_references_user_id ON diary_references(user_id)");
        db.execSQL("CREATE INDEX idx_diary_references_description_lower ON diary_references(description_lower)");
        db.execSQL("CREATE INDEX idx_sleep_entries_user_id ON sleep_entries(user_id)");
        db.execSQL("CREATE INDEX idx_emotion_pairs_user_id ON emotion_pairs(user_id)");
        db.execSQL("CREATE INDEX idx_emotion_entries_user_id ON emotion_entries(user_id)");
        db.execSQL("CREATE INDEX idx_emotion_entries_pair_id ON emotion_entries(emotion_pair_id)");
        db.execSQL("CREATE INDEX idx_sport_logs_user_id ON sport_logs(user_id)");

        db.execSQL("CREATE TABLE food_logs ("
                + "id TEXT PRIMARY KEY,"
                + "carbohydrates REAL NOT NULL,"
                + "kcal INTEGER NOT NULL,"
                + "date_time TEXT NOT NULL,"
                + "food_items TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "notes TEXT,"
                + "user_id TEXT,"
                + "user_name TEXT"
                + ")");
        db.execSQL("CREATE INDEX idx_food_logs_user_id ON food_logs(user_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE diary_entries ADD COLUMN start_time TEXT");
            db.execSQL("ALTER TABLE diary_entries ADD COLUMN end_time TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS sport_logs ("
                    + "id TEXT PRIMARY KEY,"
                    + "name TEXT NOT NULL,"
                    + "measurement REAL NOT NULL,"
                    + "measurement_unit TEXT NOT NULL,"
                    + "start_time TEXT NOT NULL,"
                    + "end_time TEXT NOT NULL,"
                    + "date TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sport_logs_user_id ON sport_logs(user_id)");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS food_logs ("
                    + "id TEXT PRIMARY KEY,"
                    + "carbohydrates REAL NOT NULL,"
                    + "kcal INTEGER NOT NULL,"
                    + "date_time TEXT NOT NULL,"
                    + "food_items TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "user_id TEXT,"
                    + "user_name TEXT"
                    + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_food_logs_user_id ON food_logs(user_id)");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
