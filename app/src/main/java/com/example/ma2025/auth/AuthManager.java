package com.example.ma2025.auth;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.ma2025.activity.MainActivity;
import com.example.ma2025.database.DatabaseHelper;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    private static FirebaseAuth getAuthInstance(Context context) {
        FirebaseApp.initializeApp(context);
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

                                    UserRepository userRepo = new UserRepository(context);
                                    User newUser = new User(email, username, password, avatar);
                                    newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));
                                    newUser.setActive(false);
                                    userRepo.createUser(newUser);

                                    ((Activity) context).finish();
                                } else {
                                    user.delete().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Toast.makeText(context, "Greška pri slanju verifikacionog email-a. Pokušaj ponovo.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        UserRepository userRepo = new UserRepository(context);
                        User localUser = userRepo.getUserByEmail(email);

                        // --- PROVERA 24 SATA ---
                        long now = System.currentTimeMillis();
                        long createdAt = 0;
                        try {
                            createdAt = Long.parseLong(localUser.getCreatedAt());
                        } catch (Exception e) {
                            createdAt = 0;
                        }
                        long hours24 = 24L * 60 * 60 * 1000;
                        boolean isExpired = (now - createdAt) > hours24;

                        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                            Toast.makeText(context, "Uspesno logovanje!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);

                        } else if (isExpired) {
                            Toast.makeText(context, "Aktivacioni link je istekao! Registruj se ponovo.", Toast.LENGTH_LONG).show();
                            firebaseUser.delete();
                            userRepo.deleteUser(localUser.getId());

                        } else {
                            Toast.makeText(context, "Email nije verifikovan! Proveri inbox.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(context, "Greska pri logovanju: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

