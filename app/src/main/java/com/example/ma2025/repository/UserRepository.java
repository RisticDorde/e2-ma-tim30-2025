// repository/UserRepository.java
package com.example.ma2025.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.database.DatabaseHelper;
import com.example.ma2025.model.Clothing;
import com.example.ma2025.model.Potion;
import com.example.ma2025.model.User;
import com.example.ma2025.model.Weapon;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private DatabaseHelper dbHelper;
    private Gson gson;

    public UserRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.gson = new Gson();
    }

    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_IS_ACTIVE, user.isActive() ? 1 : 0);

        // Profilni podaci
        values.put(DatabaseHelper.COL_LEVEL, user.getLevelNumber());
        values.put(DatabaseHelper.COL_TITLE, user.getTitle().name());
        values.put(DatabaseHelper.COL_POWER_POINTS, user.getPowerPoints());
        values.put(DatabaseHelper.COL_EXPERIENCE_POINTS, user.getExperiencePoints());
        values.put(DatabaseHelper.COL_COINS, user.getCoins());
        values.put(DatabaseHelper.COL_BADGES, gson.toJson(user.getBadges()));
        values.put(DatabaseHelper.COL_EQUIPMENT, gson.toJson(user.getEquipment()));
        values.put(DatabaseHelper.COL_CURRENT_EQUIPMENT, gson.toJson(user.getCurrentEquipment()));
        values.put(DatabaseHelper.COL_QR_CODE, user.getQrCode());
        values.put(DatabaseHelper.COL_POTIONS, gson.toJson(user.getPotions()));
        values.put(DatabaseHelper.COL_WEAPONS, gson.toJson(user.getWeapons()));
        values.put(DatabaseHelper.COL_CLOTHINGS, gson.toJson(user.getClothings()));


        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }


    public User getUserById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * Ažurira korisnički profil
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_IS_ACTIVE, user.isActive() ? 1 : 0);

        // Profilni podaci
        values.put(DatabaseHelper.COL_LEVEL, user.getLevelNumber());
        values.put(DatabaseHelper.COL_TITLE, user.getTitle().name());
        values.put(DatabaseHelper.COL_POWER_POINTS, user.getPowerPoints());
        values.put(DatabaseHelper.COL_EXPERIENCE_POINTS, user.getExperiencePoints());
        values.put(DatabaseHelper.COL_COINS, user.getCoins());
        values.put(DatabaseHelper.COL_BADGES, gson.toJson(user.getBadges()));
        values.put(DatabaseHelper.COL_EQUIPMENT, gson.toJson(user.getEquipment()));
        values.put(DatabaseHelper.COL_CURRENT_EQUIPMENT, gson.toJson(user.getCurrentEquipment()));
        values.put(DatabaseHelper.COL_QR_CODE, user.getQrCode());
        values.put(DatabaseHelper.COL_POTIONS, gson.toJson(user.getPotions()));
        values.put(DatabaseHelper.COL_WEAPONS, gson.toJson(user.getWeapons()));
        values.put(DatabaseHelper.COL_CLOTHINGS, gson.toJson(user.getClothings()));


        int result = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(user.getId())});

        db.close();
        return result > 0;
    }

    /**
     * Menja lozinku korisnika
     */
    public boolean changePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PASSWORD, newPassword);

        int result = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return result > 0;
    }

    public boolean changePasswordByEmail(String email, String newPassword) {
        User user = getUserByEmail(email);
        if (user == null) return false;

        return changePassword(user.getId(), newPassword);
    }

    public User getCurrentAppUser(Context context) {
        FirebaseUser fbUser = AuthManager.getCurrentUser(context);
        if (fbUser == null) return null;

        String email = fbUser.getEmail();
        if (email == null) return null;

        return getUserByEmail(email);
    }


    /**
     * Dodaje XP korisniku
     */
    public boolean addExperience(int userId, int xp) {
        User user = getUserById(userId);
        if (user == null) return false;

        user.addExperience(xp);
        return updateUser(user);
    }

    /**
     * Dodaje novčiće korisniku
     */
    public boolean addCoins(int userId, int coins) {
        User user = getUserById(userId);
        if (user == null) return false;

        user.addCoins(coins);
        return updateUser(user);
    }

    /**
     * Dodaje bedž korisniku
     */
    public boolean addBadge(int userId, String badgeName) {
        User user = getUserById(userId);
        if (user == null) return false;

        List<String> badges = user.getBadges();
        if (!badges.contains(badgeName)) {
            badges.add(badgeName);
            user.setBadges(badges);
            return updateUser(user);
        }
        return true; // Bedž već postoji
    }

    /**
     * Dodaje opremu korisniku
     */
    public boolean addEquipment(int userId, String equipment) {
        User user = getUserById(userId);
        if (user == null) return false;

        List<String> equipmentList = user.getEquipment();
        if (!equipmentList.contains(equipment)) {
            equipmentList.add(equipment);
            user.setEquipment(equipmentList);
            return updateUser(user);
        }
        return true; // Oprema već postoji
    }

    /**
     * Equip-uje opremu (stavlja je u trenutno korišćenu)
     */
    public boolean equipItem(int userId, String item) {
        User user = getUserById(userId);
        if (user == null) return false;

        List<String> equipment = user.getEquipment();
        List<String> currentEquipment = user.getCurrentEquipment();

        if (equipment.contains(item) && !currentEquipment.contains(item)) {
            currentEquipment.add(item);
            user.setCurrentEquipment(currentEquipment);
            return updateUser(user);
        }
        return false;
    }

    /**
     * Unequip-uje opremu
     */
    public boolean unequipItem(int userId, String item) {
        User user = getUserById(userId);
        if (user == null) return false;

        List<String> currentEquipment = user.getCurrentEquipment();
        if (currentEquipment.contains(item)) {
            currentEquipment.remove(item);
            user.setCurrentEquipment(currentEquipment);
            return updateUser(user);
        }
        return false;
    }

    /**
     * Proverava da li email već postoji
     */
    public boolean emailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_ID},
                DatabaseHelper.COL_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();

        return exists;
    }

    /**
     * Proverava da li username već postoji
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_ID},
                DatabaseHelper.COL_USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();

        return exists;
    }

    /**
     * Briše korisnika
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int result = db.delete(DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return result > 0;
    }

    /**
     * Pomocna metoda za konverziju Cursor -> User
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD)));
        user.setAvatar(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR)));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IS_ACTIVE)) == 1);
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CREATED_AT)));

        // Profilni podaci sa enum konverzijom
        int levelNumber = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LEVEL));
        user.setLevel(User.Level.fromLevelNumber(levelNumber));

        String titleValue = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE));
        try {
            User.Title title = User.Title.valueOf(titleValue);
            user.setTitle(title);
        } catch (IllegalArgumentException e) {
            user.setTitle(User.Title.NOVICE); // default
        }

        user.setPowerPoints(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_POWER_POINTS)));
        user.setExperiencePoints(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPERIENCE_POINTS)));
        user.setCoins(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COINS)));
        user.setQrCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QR_CODE)));

        // JSON array konverzije
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();

        String badgesJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGES));
        if (badgesJson != null && !badgesJson.isEmpty()) {
            user.setBadges(gson.fromJson(badgesJson, listType));
        }

        String equipmentJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT));
        if (equipmentJson != null && !equipmentJson.isEmpty()) {
            user.setEquipment(gson.fromJson(equipmentJson, listType));
        }

        String currentEquipmentJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_EQUIPMENT));
        if (currentEquipmentJson != null && !currentEquipmentJson.isEmpty()) {
            user.setCurrentEquipment(gson.fromJson(currentEquipmentJson, listType));
        }

        String potionsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_POTIONS));
        if (potionsJson != null && !potionsJson.isEmpty()) {
            Type potionListType = new TypeToken<ArrayList<Potion>>(){}.getType();
            user.setPotions(gson.fromJson(potionsJson, potionListType));
        }

        String weaponsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WEAPONS));
        if (weaponsJson != null && !weaponsJson.isEmpty()) {
            Type weaponListType = new TypeToken<ArrayList<Weapon>>(){}.getType();
            user.setWeapons(gson.fromJson(weaponsJson, weaponListType));
        }

        String clothingsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLOTHINGS));
        if (clothingsJson != null && !clothingsJson.isEmpty()) {
            Type clothingListType = new TypeToken<ArrayList<Clothing>>(){}.getType();
            user.setClothings(gson.fromJson(clothingsJson, clothingListType));
        }


        return user;
    }

    /**
     * Dobija sve korisnike (za admin panel)
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return users;
    }

    /**
     * Dobija javni profil korisnika (podaci vidljivi drugima)
     */
    public User getPublicProfile(int userId) {
        User user = getUserById(userId);
        if (user == null) return null;

        // Kreiraj kopiju sa samo javnim podacima
        User publicUser = new User();
        publicUser.setId(user.getId());
        publicUser.setUsername(user.getUsername());
        publicUser.setAvatar(user.getAvatar());
        publicUser.setLevel(user.getLevel());
        publicUser.setTitle(user.getTitle());
        publicUser.setExperiencePoints(user.getExperiencePoints());
        publicUser.setBadges(user.getBadges());
        publicUser.setCurrentEquipment(user.getCurrentEquipment());
        publicUser.setQrCode(user.getQrCode());

        return publicUser;
    }
}