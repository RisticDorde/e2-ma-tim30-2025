package com.example.ma2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.auth.LoginActivity;
import com.example.ma2025.category.CategoryActivity;
import com.example.ma2025.task.AddTaskActivity;
import com.example.ma2025.task.TaskCalendarActivity;
import com.example.ma2025.task.TaskListActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicijalizuj Firebase
        FirebaseApp.initializeApp(this);

        // Proveri da li je korisnik ulogovan i email verifikovan
        FirebaseUser currentUser = AuthManager.getCurrentUser(this);

        if (currentUser != null && currentUser.isEmailVerified()) {
            // Ulogovan korisnik → možeš ovde postaviti svoj Home layout
            setContentView(R.layout.activity_main); // može biti Hello World ili kasnije Home
            Button btnCategories = findViewById(R.id.btnCategories);
            btnCategories.setOnClickListener(v -> {
                Intent intent = new Intent(this, CategoryActivity.class);
                startActivity(intent);
            });

            Button btnAddTask = findViewById(R.id.btnAddTask);
            btnAddTask.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddTaskActivity.class);
                startActivity(intent);
            });

            Button btnTaskList = findViewById(R.id.btnTaskList);
            btnTaskList.setOnClickListener(v -> {
                Intent intent = new Intent(this, TaskListActivity.class);
                startActivity(intent);
            });

            Button btnTaskCalendar = findViewById(R.id.btnTaskCalendar);
            btnTaskCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(this, TaskCalendarActivity.class);
                startActivity(intent);
            });

        } else {
            // Nije ulogovan → idi na LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // završi MainActivity da korisnik ne može da se vrati na njega
        }
    }
}