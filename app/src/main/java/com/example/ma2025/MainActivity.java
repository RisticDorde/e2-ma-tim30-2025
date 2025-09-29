package com.example.ma2025;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.auth.LoginActivity;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Proveri da li je korisnik ulogovan i email verifikovan
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            // Ulogovan korisnik → možeš ovde postaviti svoj Home layout
            setContentView(R.layout.activity_main); // može biti Hello World ili kasnije Home
        } else {
            // Nije ulogovan → idi na LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // završi MainActivity da korisnik ne može da se vrati na njega
        }
    }
}