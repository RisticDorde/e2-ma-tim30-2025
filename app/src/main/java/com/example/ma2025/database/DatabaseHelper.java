package com.example.ma2025.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 2;

    // tabela users
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_AVATAR = "avatar";
    public static final String COL_IS_ACTIVE = "is_active";
    public static final String COL_CREATED_AT = "created_at";

    public static final String COL_LEVEL = "level";
    public static final String COL_TITLE = "title";
    public static final String COL_POWER_POINTS = "power_points";
    public static final String COL_EXPERIENCE_POINTS = "experience_points";
    public static final String COL_COINS = "coins";
    public static final String COL_BADGES = "badges";
    public static final String COL_EQUIPMENT = "equipment";
    public static final String COL_CURRENT_EQUIPMENT = "current_equipment";
    public static final String COL_QR_CODE = "qr_code";
    public static final String COL_POTIONS = "potions";
    public static final String COL_WEAPONS = "weapons";
    public static final String COL_CLOTHINGS = "clothings";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_AVATAR + " TEXT, " +
                COL_IS_ACTIVE + " INTEGER DEFAULT 0, " +
                COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL_LEVEL + " INTEGER DEFAULT 1, " +
                COL_TITLE + " TEXT DEFAULT 'Početnik', " +
                COL_POWER_POINTS + " INTEGER DEFAULT 10, " +
                COL_EXPERIENCE_POINTS + " INTEGER DEFAULT 0, " +
                COL_COINS + " INTEGER DEFAULT 0, " +
                COL_BADGES + " TEXT DEFAULT '', " +
                COL_EQUIPMENT + " TEXT DEFAULT '', " +
                COL_CURRENT_EQUIPMENT + " TEXT DEFAULT '', " + // JSON array kao string
                COL_QR_CODE + " TEXT DEFAULT ''," +
                COL_POTIONS + " TEXT DEFAULT '', " +
                COL_WEAPONS + " TEXT DEFAULT '', " +
                COL_CLOTHINGS + " TEXT DEFAULT ''" +
                ")";
        db.execSQL(CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
            String equipment = cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT));
            String currentEquipment = cursor.getString(cursor.getColumnIndexOrThrow(COL_CURRENT_EQUIPMENT));
            String qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COL_QR_CODE));
            String potions = cursor.getString(cursor.getColumnIndexOrThrow(COL_POTIONS));
            String weapons = cursor.getString(cursor.getColumnIndexOrThrow(COL_WEAPONS));
            String clothings = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLOTHINGS));
            String xp = cursor.getString((cursor.getColumnIndexOrThrow(COL_EXPERIENCE_POINTS)));

            android.util.Log.d("DB_USER",
                    "ID: " + id +
                            " | Email: " + email +
                            " | Username: " + username +
                            " | Equipment: " + equipment +
                            " | Current Equipment: " + currentEquipment +
                            " | QR Code: " + qrCode +
                            " | Potions: " + potions +
                            " | Weapons: " + weapons +
                            " | Clothings: " + clothings +
                            " | XP: " + xp
            );
        }
        cursor.close();
    }

}
