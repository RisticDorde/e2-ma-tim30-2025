package com.example.ma2025.auth;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.ma2025.MainActivity;
import com.example.ma2025.database.DatabaseHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    // NIJE STATIC INICIJALIZACIJA
    private static FirebaseAuth getAuthInstance(Context context) {
        FirebaseApp.initializeApp(context); // inicijalizuj Firebase ako već nije
        return FirebaseAuth.getInstance();
    }

    public static void registerUser(Context context, String email, String password, String username, int avatar) {
        FirebaseAuth mAuth = getAuthInstance(context);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(context, "Proveri email za aktivaciju!", Toast.LENGTH_SHORT).show();

                                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put(DatabaseHelper.COL_EMAIL, email);
                                    values.put(DatabaseHelper.COL_USERNAME, username);
                                    values.put(DatabaseHelper.COL_PASSWORD, password);
                                    values.put(DatabaseHelper.COL_AVATAR, avatar);
                                    values.put(DatabaseHelper.COL_IS_ACTIVE, 0);
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

    public static void loginUser(Context context, String email, String password) {
        FirebaseAuth mAuth = getAuthInstance(context);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(context, "Uspešno logovanje!", Toast.LENGTH_SHORT).show();
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

    public static void logoutUser(Context context) {
        FirebaseAuth mAuth = getAuthInstance(context);
        mAuth.signOut();
        Toast.makeText(context, "Odjavljeni ste.", Toast.LENGTH_SHORT).show();
    }

    public static FirebaseUser getCurrentUser(Context context) {
        FirebaseAuth mAuth = getAuthInstance(context);
        return mAuth.getCurrentUser();
    }
}

