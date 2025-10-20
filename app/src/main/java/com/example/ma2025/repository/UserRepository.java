package com.example.ma2025.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.database.DatabaseHelper;
import com.example.ma2025.model.Clothing;
import com.example.ma2025.model.Potion;
import com.example.ma2025.model.User;
import com.example.ma2025.model.Weapon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private DatabaseHelper dbHelper;
    private Gson gson;
    private FirebaseFirestore firestore;

    public UserRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.gson = new Gson();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_LEVEL, user.getLevel().name());
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
        values.put(DatabaseHelper.COL_CURRENT_BOSS_INDEX, user.getCurrentBossIndex());
        values.put(DatabaseHelper.COL_BOSS_REMAINING_HP, user.getBossRemainingHp());
        values.put(DatabaseHelper.COL_LAST_LEVEL_UP, user.getLastLevelUpAt());

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();

        // Upis i u Firestore
        saveUserToFirestore(user);

        return result;
    }

    private void saveUserToFirestore(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("password", user.getPassword());
        userMap.put("avatar", user.getAvatar());
        userMap.put("isActive", user.isActive());
        userMap.put("level", user.getLevel().name());
        userMap.put("title", user.getTitle().name());
        userMap.put("powerPoints", user.getPowerPoints());
        userMap.put("experiencePoints", user.getExperiencePoints());
        userMap.put("coins", user.getCoins());

        // Promenjeno: direktne liste, ne JSON string
        userMap.put("badges", user.getBadges());
        userMap.put("equipment", user.getEquipment());
        userMap.put("currentEquipment", user.getCurrentEquipment());
        userMap.put("qrCode", user.getQrCode());
        userMap.put("potions", user.getPotions());
        userMap.put("weapons", user.getWeapons());
        userMap.put("clothings", user.getClothings());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("current_boss_index", user.getCurrentBossIndex());
        userMap.put("boss_remaining_hp", user.getBossRemainingHp());
        userMap.put("last_level_up_at", user.getCreatedAt());

        firestore.collection("users")
                .document(user.getEmail())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Opcionalno: Log success
                })
                .addOnFailureListener(e -> {
                    // Opcionalno: Log failure
                });
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

    public boolean updateUser(User user) {
        Log.d("UPDATE", "*****************************Updating user with ID=" + user.getId());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_IS_ACTIVE, user.isActive() ? 1 : 0);
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
        values.put(DatabaseHelper.COL_CURRENT_BOSS_INDEX, user.getCurrentBossIndex());
        values.put(DatabaseHelper.COL_BOSS_REMAINING_HP, user.getBossRemainingHp());
        values.put(DatabaseHelper.COL_LAST_LEVEL_UP, user.getLastLevelUpAt());
        Log.d("UPDATE", "BossIndex = " + user.getCurrentBossIndex() +
                ", BossHP = " + user.getBossRemainingHp() +
                ", Level = " + user.getLevelNumber());
        int result = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );

        Log.d("UPDATE", "#################################Result = " + result);

        db.close();

        if (result > 0) {
            updateUserInFirestore(user);
        }

        return result > 0;
    }

    private void updateUserInFirestore(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", user.isActive());
        updates.put("level", user.getLevel().name());
        updates.put("title", user.getTitle().name());
        updates.put("powerPoints", user.getPowerPoints());
        updates.put("experiencePoints", user.getExperiencePoints());
        updates.put("coins", user.getCoins());

        // Promenjeno: direktne liste
        updates.put("badges", user.getBadges());
        updates.put("equipment", user.getEquipment());
        updates.put("currentEquipment", user.getCurrentEquipment());
        updates.put("qrCode", user.getQrCode());
        updates.put("potions", gson.toJson(user.getPotions()));
        updates.put("weapons", gson.toJson(user.getWeapons()));
        updates.put("clothings", gson.toJson(user.getClothings()));
        updates.put("current_boss_index", gson.toJson(user.getCurrentBossIndex()));
        updates.put("boss_remaining_hp", gson.toJson(user.getBossRemainingHp()));
        updates.put("last_level_up_at", gson.toJson(user.getLastLevelUpAt()));

        firestore.collection("users")
                .document(user.getEmail())
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Korisnik uspešno ažuriran u Firestore."))
                .addOnFailureListener(e -> Log.e("Firestore", "Greška pri ažuriranju korisnika: " + e.getMessage()));
    }

    /**
     * Menja lozinku korisnika
     */
    public boolean changePassword(String email, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PASSWORD, newPassword);

        int result = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COL_EMAIL + " = ?",
                new String[]{email}
        );

        db.close();

        if (result > 0) {
            // 1. Ažuriraj lozinku i u Firestore
            updatePasswordInFirestore(email, newPassword);

            // 2. Ažuriraj lozinku i u Firebase Authentication
            updatePasswordInFirebaseAuth(newPassword);
        }

        return result > 0;
    }

    private void updatePasswordInFirebaseAuth(String newPassword) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        // Lozinka uspešno promenjena
                    })
                    .addOnFailureListener(e -> {
                        // Na primer, traži ponovnu prijavu ako je sesija stara
                        // e.getMessage() može biti "Recent login required"
                    });
        }
    }

    private void updatePasswordInFirestore(String email, String newPassword) {
        User user = getUserByEmail(email);
        if (user == null) return;

        firestore.collection("users")
                .document(user.getEmail())
                .update("password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    // success
                })
                .addOnFailureListener(e -> {
                    // handle failure
                });
    }


    public boolean changePasswordByEmail(String email, String newPassword) {
        User user = getUserByEmail(email);
        if (user == null) return false;

        return changePassword(email, newPassword);
    }

    public User getCurrentAppUser(Context context) {
        FirebaseUser fbUser = AuthManager.getCurrentUser(context);
        if (fbUser == null) return null;

        String email = fbUser.getEmail();
        if (email == null) return null;

        return getUserByEmail(email);
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
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR)));
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

        user.setCurrentBossIndex(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CURRENT_BOSS_INDEX)));
        user.setBossRemainingHp(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOSS_REMAINING_HP)));
        user.setLastLevelUpAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_LEVEL_UP)));

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