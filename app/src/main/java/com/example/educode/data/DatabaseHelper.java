package com.example.educode.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.DatabaseUtils;
import android.util.Log;

import com.example.educode.models.DetectionResult;
import com.example.educode.models.Category;
import com.example.educode.models.Question;
import com.example.educode.models.Sign;
import com.example.educode.models.Badge;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Main Database Controller for the EduCode application.
 * Handles user authentication, sign data, quiz management, and learning progress tracking.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- DATABASE CONFIGURATION ---
    public static final String DATABASE_NAME = "EduCode.db";
    public static final int DATABASE_VERSION = 27;

    // --- TABLE: USERS ---
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_SCORE = "score";
    public static final String COL_SIGNS_LEARNED = "signs_learned";

    // --- TABLE: CATEGORIES ---
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_ICON = "icon_name";

    // --- TABLE: SIGNS (Road Sign Dictionary) ---
    public static final String TABLE_SIGNS = "signs";
    public static final String COL_SIGN_ID = "id";
    public static final String COL_SIGN_CAT_ID = "category_id";
    public static final String COL_SIGN_NAME = "name";
    public static final String COL_SIGN_DESC = "description";
    public static final String COL_SIGN_IMAGE = "image";
    public static final String COL_SIGN_FAV = "is_favorite";

    // --- TABLE: QUIZ QUESTIONS ---
    public static final String TABLE_QUIZ = "quiz_questions";
    public static final String COL_QUIZ_ID = "id";
    public static final String COL_QUIZ_QUESTION = "question";
    public static final String COL_QUIZ_OPT_A = "optionA";
    public static final String COL_QUIZ_OPT_B = "optionB";
    public static final String COL_QUIZ_OPT_C = "optionC";
    public static final String COL_QUIZ_CORRECT = "correctOption";
    public static final String COL_QUIZ_IMAGE = "image";

    // --- TABLE: QUIZ RESULTS (Scores/History) ---
    public static final String TABLE_QUIZ_RESULTS = "quiz_results";
    public static final String COL_RES_ID = "id";
    public static final String COL_RES_USER = "username";
    public static final String COL_RES_SCORE = "score";
    public static final String COL_RES_DATE = "timestamp";

    // --- TABLE: DETECTIONS (AI Camera History) ---
    public static final String TABLE_DETECTIONS = "detections";
    public static final String COL_DET_ID = "id";
    public static final String COL_DET_USER = "username";
    public static final String COL_DET_SIGN_ID = "sign_id";
    public static final String COL_DET_CONFIDENCE = "confidence";
    public static final String COL_DET_DATE = "timestamp";

    // --- TABLE: FAVORITES ---
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COL_FAV_ID = "id";
    public static final String COL_FAV_USER = "username";
    public static final String COL_FAV_SIGN_ID = "sign_id";

    // --- TABLES: GAMIFICATION (Badges) ---
    public static final String TABLE_BADGES = "badges";
    public static final String COL_BADGE_ID = "id";
    public static final String COL_BADGE_NAME = "name";
    public static final String COL_BADGE_DESC = "description";
    public static final String COL_BADGE_ICON = "icon";

    public static final String TABLE_USER_BADGES = "user_badges";
    public static final String COL_UB_USER = "username";
    public static final String COL_UB_BADGE_ID = "badge_id";

    // --- TABLE: MISTAKES (Learning Analytics) ---
    private static final String TABLE_MISTAKES = "mistakes";
    private static final String COL_MISTAKE_ID = "id";
    private static final String COL_MISTAKE_USER = "username";
    private static final String COL_MISTAKE_SIGN = "sign_name";
    private static final String COL_MISTAKE_DATE = "date_added";

    // --- TABLE: LEGACY/UNUSED ---
    public static final String TABLE_HISTORY = "history";
    public static final String COL_HIST_ID = "id";
    public static final String COL_HIST_LABEL = "label";
    public static final String COL_HIST_DATE = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // --- DATABASE LIFECYCLE ---

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_SCORE + " INTEGER DEFAULT 0, " +
                COL_SIGNS_LEARNED + " INTEGER DEFAULT 0)");

        // Categories
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT, " +
                COL_CAT_ICON + " TEXT)");

        // Signs
        db.execSQL("CREATE TABLE " + TABLE_SIGNS + " (" +
                COL_SIGN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SIGN_CAT_ID + " INTEGER, " +
                COL_SIGN_NAME + " TEXT, " +
                COL_SIGN_DESC + " TEXT, " +
                COL_SIGN_IMAGE + " TEXT, " +
                COL_SIGN_FAV + " INTEGER DEFAULT 0)");

        // Quiz
        db.execSQL("CREATE TABLE " + TABLE_QUIZ + " (" +
                COL_QUIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_QUIZ_QUESTION + " TEXT, " +
                COL_QUIZ_OPT_A + " TEXT, " +
                COL_QUIZ_OPT_B + " TEXT, " +
                COL_QUIZ_OPT_C + " TEXT, " +
                COL_QUIZ_CORRECT + " TEXT, " +
                COL_QUIZ_IMAGE + " TEXT)");

        // Results
        db.execSQL("CREATE TABLE " + TABLE_QUIZ_RESULTS + " (" +
                COL_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RES_USER + " TEXT, " +
                COL_RES_SCORE + " INTEGER, " +
                COL_RES_DATE + " LONG)");

        // Legacy History
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HIST_LABEL + " TEXT, " +
                COL_HIST_DATE + " LONG)");

        // Multi-user Favorites
        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" +
                COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FAV_USER + " TEXT, " +
                COL_FAV_SIGN_ID + " INTEGER)");

        // Badge definitions
        db.execSQL("CREATE TABLE " + TABLE_BADGES + " (" +
                COL_BADGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BADGE_NAME + " TEXT, " +
                COL_BADGE_DESC + " TEXT, " +
                COL_BADGE_ICON + " TEXT)");

        // Junction table for User-Badge relationship
        db.execSQL("CREATE TABLE " + TABLE_USER_BADGES + " (" +
                COL_UB_USER + " TEXT, " +
                COL_UB_BADGE_ID + " INTEGER, " +
                "PRIMARY KEY (" + COL_UB_USER + ", " + COL_UB_BADGE_ID + "))");

        // Personalized Mistakes
        db.execSQL("CREATE TABLE " + TABLE_MISTAKES + " (" +
                COL_MISTAKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MISTAKE_USER + " TEXT, " +
                COL_MISTAKE_SIGN + " TEXT, " +
                COL_MISTAKE_DATE + " LONG)");

        // AI Detections
        db.execSQL("CREATE TABLE " + TABLE_DETECTIONS + " (" +
                COL_DET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DET_USER + " TEXT, " +             // Linked to the current user
                COL_DET_SIGN_ID + " INTEGER, " +
                COL_DET_CONFIDENCE + " REAL, " +
                COL_DET_DATE + " LONG)");

        // Seed initial data
        insertDefaultData(db);
        insertDefaultBadges(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SIGNS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_RESULTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MISTAKES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETECTIONS);
        onCreate(db);
    }

    // --- USER MANAGEMENT & AUTHENTICATION ---

    /**
     * Checks if username and password combination is valid.
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
        return exists;
    }

    /**
     * Registers a new user in the system.
     */
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_SCORE, 0);
        contentValues.put(COL_SIGNS_LEARNED, 0);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    /**
     * Prevents duplicate usernames during registration.
     */
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?", new String[]{username});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
        return exists;
    }

    // --- LEARNING CONTENT ACCESS (Signs & Categories) ---

    /**
     * Retrieves all sign categories.
     */
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Category(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Retrieves all signs belonging to a specific category.
     */
    public List<Sign> getSignsByCategory(int categoryId) {
        List<Sign> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_SIGNS + " WHERE " + COL_SIGN_CAT_ID + "=?", new String[]{String.valueOf(categoryId)});
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Sign(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Fetches details for a specific sign by its ID.
     */
    public Sign getSignById(int signId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Sign sign = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_SIGNS + " WHERE " + COL_SIGN_ID + "=?", new String[]{String.valueOf(signId)});
            if (cursor.moveToFirst()) {
                sign = new Sign(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return sign;
    }

    /**
     * Internal helper to find Sign ID via text search.
     */
    private int getSignIdByName(SQLiteDatabase db, String name) {
        int id = -1;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_SIGN_ID + " FROM " + TABLE_SIGNS + " WHERE " + COL_SIGN_NAME + " LIKE ?", new String[]{name});
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return id;
    }

    // --- QUIZ LOGIC ---

    /**
     * Fetches a randomized set of questions for a quiz session.
     */
    public List<Question> getRandomQuestions(int limit) {
        List<Question> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT * FROM " + TABLE_QUIZ + " ORDER BY RANDOM() LIMIT " + limit, null);
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Question(
                            cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5),
                            cursor.getString(6)
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Saves a completed quiz score to history.
     */
    public void addQuizResult(String username, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RES_USER, username);
        values.put(COL_RES_SCORE, score);
        values.put(COL_RES_DATE, System.currentTimeMillis());
        db.insert(TABLE_QUIZ_RESULTS, null, values);
    }

    // --- AI DETECTION MANAGEMENT ---

    /**
     * Saves a sign detection result from the AI camera.
     * Handles dynamic sign creation if the label doesn't exist in the DB.
     */

    public void saveDetectionRaw(String username, String signLabel, float confidence) {
        SQLiteDatabase db = this.getWritableDatabase();
        String cleanLabel = signLabel;
        if (signLabel.equalsIgnoreCase("interdit : sens")) cleanLabel = "Sens Interdit";
        if (signLabel.equalsIgnoreCase("autoroute")) cleanLabel = "Autoroute";

        int signId = getSignIdByName(db, cleanLabel);

        if (signId == -1) {
            long newId = insertDynamicSign(db, signLabel);
            signId = (int) newId;
            Log.d("DB_DEBUG", "Created new dynamic sign with ID: " + signId);
        }

        if (signId != -1) {
            ContentValues values = new ContentValues();
            values.put(COL_DET_SIGN_ID, signId);
            values.put(COL_DET_CONFIDENCE, confidence);
            values.put(COL_DET_DATE, System.currentTimeMillis());
            values.put("username", username);

            long result = db.insert(TABLE_DETECTIONS, null, values);
            if (result != -1) {
                Log.d("DB_DEBUG", "Saved detection for user: " + username + " (SignID: " + signId + ")");
            }
        }
    }

    /**
     * Fetches detection history with optional filters and search.
     */
// Inside DatabaseHelper.java
    public List<DetectionResult> getDetections(String username, int filterType, String query) {
        List<DetectionResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.").append(COL_DET_ID).append(", ");
        sql.append("s.").append(COL_SIGN_NAME).append(", ");
        sql.append("d.").append(COL_DET_CONFIDENCE).append(", ");
        sql.append("d.").append(COL_DET_DATE).append(", ");
        sql.append("s.").append(COL_SIGN_IMAGE);
        sql.append(" FROM ").append(TABLE_DETECTIONS).append(" d");
        sql.append(" LEFT JOIN ").append(TABLE_SIGNS).append(" s");
        sql.append(" ON d.").append(COL_DET_SIGN_ID).append(" = s.").append(COL_SIGN_ID);

        // Filter by the specific user
        sql.append(" WHERE d.username = ?");

        List<String> args = new ArrayList<>();
        args.add(username);

        // Add search query if provided
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND s.").append(COL_SIGN_NAME).append(" LIKE ?");
            args.add("%" + query + "%");
        }

        // Apply sorting based on user selection
        if (filterType == 1) {
            sql.append(" ORDER BY d.").append(COL_DET_CONFIDENCE).append(" DESC");
        } else {
            sql.append(" ORDER BY d.").append(COL_DET_DATE).append(" DESC");
        }

        try {
            cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            if (cursor.moveToFirst()) {
                do {
                    results.add(new DetectionResult(
                            cursor.getInt(0),
                            cursor.getString(1) == null ? "Inconnu" : cursor.getString(1),
                            cursor.getString(4) == null ? "ic_unknown" : cursor.getString(4),
                            cursor.getFloat(2),
                            cursor.getLong(3)
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return results;
    }

    // --- FAVORITES (MULTI-USER) ---

    public void addFavorite(String username, int signId) {
        if (isFavorite(username, signId)) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_USER, username);
        values.put(COL_FAV_SIGN_ID, signId);
        db.insert(TABLE_FAVORITES, null, values);
    }

    public void removeFavorite(String username, int signId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COL_FAV_USER + "=? AND " + COL_FAV_SIGN_ID + "=?", new String[]{username, String.valueOf(signId)});
    }

    public boolean isFavorite(String username, int signId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITES + " WHERE " + COL_FAV_USER + "=? AND " + COL_FAV_SIGN_ID + "=?", new String[]{username, String.valueOf(signId)});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
        return exists;
    }

    public List<Sign> getUserFavorites(String username) {
        List<Sign> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT s.* FROM " + TABLE_SIGNS + " s " +
                    "JOIN " + TABLE_FAVORITES + " f ON s." + COL_SIGN_ID + " = f." + COL_FAV_SIGN_ID + " " +
                    "WHERE f." + COL_FAV_USER + "=?";
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Sign(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public int getFavoriteCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_FAVORITES, COL_FAV_USER + "=?", new String[]{username});
    }

    // --- GAMIFICATION: BADGES ---

    public List<Badge> getAllBadgesWithStatus(String username) {
        List<Badge> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_BADGES, null);
            if (cursor.moveToFirst()) {
                do {
                    int badgeId = cursor.getInt(0);
                    String name = cursor.getString(1);
                    String desc = cursor.getString(2);
                    String icon = cursor.getString(3);
                    boolean unlocked = checkUserHasBadge(username, badgeId);
                    list.add(new Badge(badgeId, name, desc, icon, unlocked));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public void unlockBadge(String username, int badgeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!checkUserHasBadge(username, badgeId)) {
            ContentValues values = new ContentValues();
            values.put(COL_UB_USER, username);
            values.put(COL_UB_BADGE_ID, badgeId);
            db.insert(TABLE_USER_BADGES, null, values);
        }
    }

    public boolean checkUserHasBadge(String username, int badgeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        boolean hasBadge = false;
        try {
            c = db.rawQuery("SELECT * FROM " + TABLE_USER_BADGES + " WHERE " + COL_UB_USER + "=? AND " + COL_UB_BADGE_ID + "=?", new String[]{username, String.valueOf(badgeId)});
            hasBadge = c.getCount() > 0;
        } finally {
            if (c != null) c.close();
        }
        return hasBadge;
    }

    // --- USER STATISTICS & PROGRESS TRACKING ---

    /**
     * Increments the count of signs "learned" when a user views a detail page.
     */
    public void incrementSignsLearned(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_USERS + " SET " + COL_SIGNS_LEARNED + " = " + COL_SIGNS_LEARNED + " + 1 WHERE " + COL_USERNAME + " = ?", new String[]{username});
            Cursor cursor = db.rawQuery("SELECT changes()", null);
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                ContentValues values = new ContentValues();
                values.put(COL_USERNAME, username);
                values.put(COL_SIGNS_LEARNED, 1);
                values.put(COL_SCORE, 0);
                db.insert(TABLE_USERS, null, values);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DB_DEBUG", "Error incrementing: " + e.getMessage());
        }
    }

    /**
     * Updates the persistent global XP score for the user.
     */
    public void updateUserScore(String username, int newScoreToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            int currentScore = 0;
            cursor = db.rawQuery("SELECT " + COL_SCORE + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?", new String[]{username});
            if (cursor.moveToFirst()) {
                currentScore = cursor.getInt(0);
            }
            int finalScore = currentScore + newScoreToAdd;
            ContentValues values = new ContentValues();
            values.put(COL_SCORE, finalScore);
            db.update(TABLE_USERS, values, COL_USERNAME + "=?", new String[]{username});
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int[] getUserStats(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int[] stats = new int[]{0, 0};
        try {
            cursor = db.rawQuery("SELECT " + COL_SCORE + ", " + COL_SIGNS_LEARNED + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?", new String[]{username});
            if (cursor.moveToFirst()) {
                stats[0] = cursor.getInt(0);
                stats[1] = cursor.getInt(1);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return stats;
    }

    public int getBestScore(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int best = 0;
        try {
            cursor = db.rawQuery("SELECT MAX(" + COL_RES_SCORE + ") FROM " + TABLE_QUIZ_RESULTS + " WHERE " + COL_RES_USER + "=?", new String[]{username});
            if (cursor.moveToFirst()) {
                best = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return best;
    }

    // --- DASHBOARD ANALYTICS ---

    /**
     * Calculates activity data for the 7-day bar chart (Quizzes + Detections).
     */
    public int[] getWeeklyStats(String username) {
        int[] stats = new int[7];
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String sql = "SELECT strftime('%w', timestamp/1000, 'unixepoch', 'localtime') as day, COUNT(*) " +
                    "FROM (" +
                    "SELECT " + COL_RES_DATE + " as timestamp FROM " + TABLE_QUIZ_RESULTS + " WHERE " + COL_RES_USER + "=? " +
                    "UNION ALL " +
                    "SELECT " + COL_DET_DATE + " as timestamp FROM " + TABLE_DETECTIONS + " WHERE username=?" +
                    ") GROUP BY day";

            cursor = db.rawQuery(sql, new String[]{username, username});
            if (cursor.moveToFirst()) {
                do {
                    int dayIndex = cursor.getInt(0);
                    int count = cursor.getInt(1);
                    int arrayIndex = (dayIndex + 6) % 7; // Map Sun=0 to last index
                    if (arrayIndex >= 0 && arrayIndex < 7) {
                        stats[arrayIndex] = count;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return stats;
    }

    /**
     * Calculates consecutive days of activity for the user.
     */
    public int getCurrentStreak(String username) {
        int streak = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        String query = "SELECT DISTINCT date(" + COL_RES_DATE + "/1000, 'unixepoch') as activity_date " +
                "FROM " + TABLE_QUIZ_RESULTS + " WHERE " + COL_RES_USER + " = ? " +
                " ORDER BY activity_date DESC";

        try {
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                streak = 1;
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_MONTH, -1);
                do {
                    String activityDate = cursor.getString(0);
                    String expectedDate = sdf.format(cal.getTime());
                    if (activityDate.equals(expectedDate)) {
                        streak++;
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error calculating streak: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return streak;
    }

    /**
     * Logs an incorrect answer during a quiz to show later as a review.
     */
    public void logMistake(String username, String signName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_MISTAKES + " WHERE " + COL_MISTAKE_USER + "=? AND " + COL_MISTAKE_SIGN + "=?", new String[]{username, signName});
            if (cursor.getCount() > 0) return;

            ContentValues values = new ContentValues();
            values.put(COL_MISTAKE_USER, username);
            values.put(COL_MISTAKE_SIGN, signName);
            values.put(COL_MISTAKE_DATE, System.currentTimeMillis());
            db.insert(TABLE_MISTAKES, null, values);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<String> getRecentMistakes(String username) {
        List<String> mistakes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT " + COL_MISTAKE_SIGN + " FROM " + TABLE_MISTAKES +
                    " WHERE " + COL_MISTAKE_USER + " = ? GROUP BY " + COL_MISTAKE_SIGN +
                    " ORDER BY MAX(" + COL_MISTAKE_DATE + ") DESC LIMIT 3";
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                do {
                    mistakes.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return mistakes;
    }

    // --- DATA SEEDING (DEFAULT CONTENT) ---

    private void insertDefaultData(SQLiteDatabase db) {
        // Categories
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux de Danger (A)', 'ic_danger')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux d’Interdiction (B)', 'ic_prohibition')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux d’Obligation (B)', 'ic_mandatory')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux d’Indication (C)', 'ic_indication')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux de Priorité (AB)', 'ic_priority')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux de Direction (D)', 'ic_direction')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux de Services', 'ic_service')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, icon_name) VALUES ('Panneaux Temporaires', 'ic_temporary')");

        // Quiz Questions
        insertQuiz(db, "Que signifie ce panneau ?", "Arrêt obligatoire", "Cédez le passage", "Sens interdit", "A", "sign_stop");
        insertQuiz(db, "Quelle est la vitesse limite ici ?", "30 km/h", "50 km/h", "70 km/h", "B", "sign_speed_limit");
        insertQuiz(db, "Ce panneau indique :", "Une école", "Un passage piéton", "Des travaux", "B", "sign_pedestrian");
        insertQuiz(db, "Que devez-vous faire ?", "Klaxonner", "Ralentir", "Accélérer", "B", "sign_danger_other");
        insertQuiz(db, "Ce panneau est de forme :", "Triangulaire", "Ronde", "Carrée", "A", "sign_yield");
        insertQuiz(db, "Quelle est cette interdiction ?", "Tourner à gauche", "Tourner à droite", "Faire demi-tour", "A", "sign_no_turn");
        insertQuiz(db, "Ce panneau bleu indique :", "Une obligation", "Une indication", "Un danger", "A", "sign_direction_mandatory");
        insertQuiz(db, "En présence de ce panneau :", "Je suis prioritaire", "Je cède le passage", "Je m'arrête", "A", "ic_priority");
        insertQuiz(db, "Ce panneau signale :", "Un dos d'âne", "Un virage", "Une chaussée glissante", "A", "sign_dos_dane");
        insertQuiz(db, "L'accès est interdit aux :", "Piétons", "Vélos", "Tous véhicules", "C", "sign_no_access");
        insertQuiz(db, "Ceci est une entrée de :", "Autoroute", "Ville", "Parking", "A", "sign_highway_indication");
        insertQuiz(db, "Couleur des panneaux temporaires ?", "Rouge", "Jaune", "Bleu", "B", "sign_temp_danger");
        insertQuiz(db, "Accès interdit dans ce sens :", "Sens unique", "Sens interdit", "Impasse", "B", "sign_no_entry");
        insertQuiz(db, "Panneau de chantier :", "Danger temporaire", "Interdiction temporaire", "Déviation", "B", "sign_temp_prohibition");
        insertQuiz(db, "Vitesse maximale dans cette zone :", "30 km/h", "50 km/h", "70 km/h", "A", "sign_zone_30");
        insertQuiz(db, "Attention, endroit fréquenté par :", "Des enfants", "Des animaux", "Des cyclistes", "A", "sign_pedestrian_indication");
        insertQuiz(db, "Ce panneau indique :", "Fin d'obligation", "Fin d'interdiction", "Fin de priorité", "A", "sign_end_mandatory");
        insertQuiz(db, "Panneau à fond marron :", "Lieu touristique", "Autoroute", "Route nationale", "A", "sign_dir_tourism");
        insertQuiz(db, "Ce panneau signale :", "Une entrée/sortie de localité", "Une rue piétonne", "Une forêt", "A", "sign_localization");
        insertQuiz(db, "Panneau à fond vert :", "Itinéraire important", "Autoroute", "Chemin de terre", "A", "sign_dir_city");
        insertQuiz(db, "Ce panneau annonce :", "Un rond-point", "Une succession de virages", "Une route déformée", "B", "sign_virage");
        insertQuiz(db, "La chaussée va :", "S'élargir", "Rétrécir", "Tourner à droite", "B", "sign_retrecissement");
        insertQuiz(db, "Annonce de feux :", "Tricolores", "Clignotants", "De passage à niveau", "A", "sign_traffic_light");
        insertQuiz(db, "Danger potentiel :", "Chute de pierres", "Passage d'animaux", "Vent fort", "B", "sign_animals");
        insertQuiz(db, "Interdiction de :", "Doubler", "Tourner", "S'arrêter", "A", "sign_no_overtaking");
        insertQuiz(db, "Ce panneau interdit :", "L'arrêt", "Le stationnement", "L'arrêt et le stationnement", "B", "sign_no_parking");
        insertQuiz(db, "Ce panneau indique :", "Fin d'interdictions", "Fin de chantier", "Fin de priorité", "A", "sign_end_prohibition");
        insertQuiz(db, "Accès interdit :", "Aux piétons", "À tous véhicules", "Aux vélos", "B", "sign_no_access");
        insertQuiz(db, "Vitesse obligatoire :", "Maximum 30", "Minimum 30", "Conseillée 30", "B", "sign_min_speed");
        insertQuiz(db, "Voie réservée aux :", "Vélos", "Camions", "Piétons", "A", "sign_mandatory_lane");
        insertQuiz(db, "Équipement requis :", "Pneus pluie", "Chaînes à neige", "Gilet jaune", "B", "sign_snow_chains");
        insertQuiz(db, "Cette rue est :", "Une impasse", "Un sens unique", "Une déviation", "A", "sign_dead_end");
        insertQuiz(db, "Lieu aménagé pour :", "Le stationnement", "Le camping", "Le pique-nique", "A", "sign_parking");
        insertQuiz(db, "Arrêt de :", "Bus", "Tramway", "Métro", "A", "sign_bus_stop");
        insertQuiz(db, "Service proche :", "Hôtel / Hôpital", "Garage", "Police", "A", "sign_hospital");
        insertQuiz(db, "Prochain service :", "Essence", "Péage", "Aire de repos", "A", "sign_gas_station");
        insertQuiz(db, "Priorité :", "Fin de priorité", "Priorité à droite", "Cédez le passage", "A", "sign_end_priority");
        insertQuiz(db, "Direction vers :", "Une autoroute", "Une nationale", "Un lieu-dit", "A", "sign_dir_highway");
        insertQuiz(db, "Suivre la flèche pour :", "La déviation", "L'autoroute", "Le centre-ville", "A", "sign_deviation");
        insertQuiz(db, "Vous entrez dans :", "Une ville", "Un département", "Une région", "A", "sign_city_entry");

        // Signs
        insertSign(db, 1, "Virages", "Annonce une série de virages dangereux.", "sign_virage");
        insertSign(db, 1, "Cassis / Dos-d’âne", "Ralentisseur ou déformation de la chaussée.", "sign_dos_dane");
        insertSign(db, 1, "Chaussée rétrécie", "La route va devenir plus étroite.", "sign_retrecissement");
        insertSign(db, 1, "Enfants", "Passage fréquent d'enfants (école, terrain de jeux).", "sign_pedestrian_indication");
        insertSign(db, 1, "Passage piéton", "Annonce un passage pour piétons.", "sign_pedestrian");
        insertSign(db, 1, "Feux tricolores", "Annonce un feu de signalisation.", "sign_traffic_light");
        insertSign(db, 1, "Animaux", "Passage d'animaux domestiques ou sauvages.", "sign_animals");
        insertSign(db, 1, "Autres dangers", "Danger non spécifié par un autre panneau.", "sign_danger_other");
        insertSign(db, 2, "Accès interdit", "Interdiction de passer pour tous les véhicules.", "sign_no_access");
        insertSign(db, 2, "Sens interdit", "Interdiction de circuler dans ce sens.", "sign_no_entry");
        insertSign(db, 2, "Interdiction de tourner", "Interdiction de tourner à gauche ou à droite.", "sign_no_turn");
        insertSign(db, 2, "Dépassement interdit", "Interdiction de dépasser tous les véhicules à moteur.", "sign_no_overtaking");
        insertSign(db, 2, "Limitation de vitesse", "Vitesse maximale autorisée.", "sign_speed_limit");
        insertSign(db, 2, "Interdiction de stationner", "Stationnement interdit.", "sign_no_parking");
        insertSign(db, 2, "Fin d’interdictions", "Fin de toutes les interdictions locales.", "sign_end_prohibition");
        insertSign(db, 3, "Direction obligatoire", "Vous devez suivre la direction indiquée.", "sign_direction_mandatory");
        insertSign(db, 3, "Vitesse minimale", "Vitesse minimale obligatoire.", "sign_min_speed");
        insertSign(db, 3, "Pistes obligatoires", "Voie réservée obligatoire (vélo, bus, etc.).", "sign_mandatory_lane");
        insertSign(db, 3, "Chaînes à neige", "Chaînes à neige obligatoires sur au moins deux roues motrices.", "sign_snow_chains");
        insertSign(db, 3, "Fin d’obligation", "Fin de l'obligation précédemment signalée.", "sign_end_mandatory");
        insertSign(db, 4, "Parking", "Lieu aménagé pour le stationnement.", "sign_parking");
        insertSign(db, 4, "Impasse", "Voie sans issue.", "sign_dead_end");
        insertSign(db, 4, "Passage piéton", "Indication d'un passage pour piétons.", "sign_pedestrian_indication");
        insertSign(db, 4, "Arrêts / transports", "Arrêt de bus, tramway ou taxi.", "sign_bus_stop");
        insertSign(db, 4, "Routes spécifiques", "Autoroute, route pour automobiles, etc.", "sign_highway_indication");
        insertSign(db, 5, "Cédez le passage", "Vous devez laisser la priorité aux véhicules venant de l'autre voie.", "sign_yield");
        insertSign(db, 5, "Stop", "Arrêt absolu obligatoire à l'intersection.", "sign_stop");
        insertSign(db, 5, "Route prioritaire", "Vous avez la priorité aux intersections.", "ic_priority");
        insertSign(db, 5, "Fin de priorité", "La route cesse d'être prioritaire.", "sign_end_priority");
        insertSign(db, 6, "Autoroute", "Direction vers une autoroute.", "sign_dir_highway");
        insertSign(db, 6, "Itinéraires touristiques", "Direction vers un lieu touristique (panneau marron).", "sign_dir_tourism");
        insertSign(db, 6, "Localisation", "Panneau d'entrée ou de sortie de ville.", "sign_localization");
        insertSign(db, 6, "Directions classiques", "Direction vers une ville importante (panneau vert).", "sign_dir_city");
        insertSign(db, 7, "Zone 30 / rencontre", "Zone où la vitesse est limitée et le piéton prioritaire.", "sign_zone_30");
        insertSign(db, 7, "Entrée / sortie agglo", "Panneau indiquant le nom de la ville.", "sign_city_entry");
        insertSign(db, 7, "Station-service", "Poste de distribution de carburant.", "sign_gas_station");
        insertSign(db, 7, "Divers services", "Hôpital, poste de secours, hôtel, etc.", "sign_hospital");
        insertSign(db, 8, "Danger temporaire", "Travaux ou danger temporaire (fond jaune).", "sign_temp_danger");
        insertSign(db, 8, "Interdiction temporaire", "Interdiction liée aux travaux.", "sign_temp_prohibition");
        insertSign(db, 8, "Déviation", "Itinéraire de déviation.", "sign_deviation");
    }

    private void insertDefaultBadges(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_BADGES + " (name, description, icon) VALUES ('Débutant', 'Atteindre 10 points d''expérience', 'ic_badge_bronze')");
        db.execSQL("INSERT INTO " + TABLE_BADGES + " (name, description, icon) VALUES ('Intermédiaire', 'Atteindre 100 points d''expérience', 'ic_badge_silver')");
        db.execSQL("INSERT INTO " + TABLE_BADGES + " (name, description, icon) VALUES ('Expert', 'Atteindre 300 points d''expérience', 'ic_badge_gold')");
        db.execSQL("INSERT INTO " + TABLE_BADGES + " (name, description, icon) VALUES ('Maître', 'Atteindre 600 points d''expérience', 'ic_badge_master')");
    }

    // --- INTERNAL HELPERS ---

    private void insertSign(SQLiteDatabase db, int catId, String name, String desc, String image) {
        String safeName = name.replace("'", "''");
        String safeDesc = desc.replace("'", "''");
        db.execSQL("INSERT INTO " + TABLE_SIGNS + " (category_id, name, description, image, is_favorite) VALUES (" +
                catId + ", '" + safeName + "', '" + safeDesc + "', '" + image + "', 0)");
    }

    private void insertQuiz(SQLiteDatabase db, String q, String a, String b, String c, String correct, String img) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT 1 FROM " + TABLE_QUIZ + " WHERE " + COL_QUIZ_QUESTION + " = ?", new String[]{q});
            if (!cursor.moveToFirst()) {
                db.execSQL("INSERT INTO " + TABLE_QUIZ + " (question, optionA, optionB, optionC, correctOption, image) VALUES (?, ?, ?, ?, ?, ?)", new Object[]{q, a, b, c, correct, img});
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    // 1. Updated to assign correct Category IDs based on the sign type
    private long insertDynamicSign(SQLiteDatabase db, String label) {
        ContentValues values = new ContentValues();

        String cleanName = label;
        int categoryId = 1; // Default to Danger (A)

        if (label.equalsIgnoreCase("interdit : sens")) {
            cleanName = "Sens Interdit";
            categoryId = 2; // Interdiction (B)
        } else if (label.equalsIgnoreCase("autoroute")) {
            cleanName = "Autoroute";
            categoryId = 4; // Indication (C)
        } else if (label.toLowerCase().contains("stationnement") || label.toLowerCase().contains("interdit")) {
            categoryId = 2; // Interdiction (B)
        } else if (label.toLowerCase().contains("passage piéton") || label.toLowerCase().contains("piétons")) {
            categoryId = 4; // Indication (C)
        }

        values.put(COL_SIGN_NAME, cleanName);
        values.put(COL_SIGN_DESC, "Détecté par l'IA");
        values.put(COL_SIGN_IMAGE, getImageResourceForLabel(label));
        values.put(COL_SIGN_CAT_ID, categoryId); // Now uses the dynamic category

        return db.insert(TABLE_SIGNS, null, values);
    }

    // 2. Refined mapping to ensure "Passage Piétons" and "Traffic Signal" use correct icons
    private String getImageResourceForLabel(String label) {
        if (label == null) return "ic_unknown";
        String lower = label.toLowerCase();

        if (lower.contains("autoroute") || lower.contains("highway")) return "sign_highway_indication";
        if (lower.contains("sens interdit") || lower.contains("interdit : sens") || lower.contains("no entry")) return "sign_no_entry";
        if (lower.contains("cedez") || lower.contains("cédez") || lower.contains("give way")) return "sign_yield";
        if (lower.contains("ecole") || lower.contains("école") || lower.contains("enfants")) return "sign_pedestrian_indication";
        if (lower.contains("stop")) return "sign_stop";
        if (lower.contains("parking")) return "sign_parking";
        if (lower.contains("30")) return "sign_speed_30";
        if (lower.contains("50")) return "sign_speed_50";

        // Fix for Stationnement Interdit
        if (lower.contains("stationnement") || lower.contains("parking interdit")) return "sign_no_parking";

        // Fix for Passage Piétons (Indication version vs Danger version)
        if (lower.contains("pieton") || lower.contains("piéton")) return "sign_pedestrian";

        // Fix for Traffic Signal
        if (lower.contains("feu") || lower.contains("traffic signal")) return "sign_traffic_light";

        if (lower.contains("virage")) return "sign_virage";
        if (lower.contains("dos") && lower.contains("ane")) return "sign_dos_dane";

        return "ic_unknown";
    }

    public int getLearnedSignsCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_SIGNS_LEARNED + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?", new String[]{username});
            if (cursor.moveToFirst()) count = cursor.getInt(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    public int getQuizCompletedCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_QUIZ_RESULTS, COL_RES_USER + "=?", new String[]{username});
    }

    public int getSignsLearnedCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT " + COL_SIGNS_LEARNED + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }
    // Count only for the specific user
    public int getDetectionCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_DETECTIONS,
                COL_DET_USER + "=?", new String[]{username});
    }
}