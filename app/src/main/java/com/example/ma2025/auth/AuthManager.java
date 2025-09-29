package com.example.ma2025.auth;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.ma2025.MainActivity;
import com.example.ma2025.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthManager {

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Registracija korisnika preko Firebase Authentication.
     * Ako uspe, šalje email verifikaciju i upisuje korisnika u SQLite.
     */
    public static void registerUser(Context context, String email, String password, String username, int avatar) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // šaljemo verifikacioni email
                            user.sendEmailVerification().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(context, "Proveri email za aktivaciju!", Toast.LENGTH_SHORT).show();

                                    // upis u SQLite (lokalna kopija korisnika)
                                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(DatabaseHelper.COL_EMAIL, email);
                                    values.put(DatabaseHelper.COL_USERNAME, username);
                                    values.put(DatabaseHelper.COL_PASSWORD, password);
                                    values.put(DatabaseHelper.COL_AVATAR, avatar);
                                    values.put(DatabaseHelper.COL_IS_ACTIVE, 0); // 0 jer nije aktiviran dok ne potvrdi email
                                    db.insert(DatabaseHelper.TABLE_USERS, null, values);
                                    db.close();
                                } else {
                                    Toast.makeText(context, "Greška pri slanju verifikacionog email-a.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(context, "Registracija neuspešna: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Login korisnika preko Firebase Authentication.
     * Proverava da li je email verifikovan.
     */
    public static void loginUser(Context context, String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(context, "Uspešno logovanje!", Toast.LENGTH_SHORT).show();

                            // idi na MainActivity
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);

                        } else {
                            Toast.makeText(context, "Email nije verifikovan!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Greška pri logovanju: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Odjava korisnika
     */
    public static void logoutUser(Context context) {
        mAuth.signOut();
        Toast.makeText(context, "Odjavljeni ste.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Vraca trenutno ulogovanog korisnika (ako postoji)
     */
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}