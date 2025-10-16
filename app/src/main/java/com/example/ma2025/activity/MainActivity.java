package com.example.ma2025.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OVO ĆE OTVORITI BAZU
        //DatabaseHelper dbHelper = new DatabaseHelper(this);
       // SQLiteDatabase db = dbHelper.getReadableDatabase();


        //Log.d("DB_DEBUG", "Database forced open");
        // Inicijalizuj Firebase
        FirebaseApp.initializeApp(this);

        // Proveri da li je korisnik ulogovan i email verifikovan
        FirebaseUser currentUser = AuthManager.getCurrentUser(this);

        if (currentUser != null && currentUser.isEmailVerified()) {
            // Ulogovan korisnik → možeš ovde postaviti svoj Home layout
            setContentView(R.layout.activity_main); // može biti Hello World ili kasnije Home

            btnProfile = findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });

            btnLogout = findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Odjavi korisnika preko AuthManager / Firebase
                    AuthManager.logoutUser(MainActivity.this);

                    // Idi na LoginActivity i očisti back stack
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            // Nije ulogovan → idi na LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // završi MainActivity da korisnik ne može da se vrati na njega
        }
    }
}