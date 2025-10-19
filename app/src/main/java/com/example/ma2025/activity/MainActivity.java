package com.example.ma2025.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.activity.EquipmentActivity;
import com.example.ma2025.activity.LoginActivity;
import com.example.ma2025.activity.ProfileActivity;
import com.example.ma2025.activity.ShopActivity;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.category.CategoryActivity;
import com.example.ma2025.task.AddTaskActivity;
import com.example.ma2025.task.TaskCalendarActivity;
import com.example.ma2025.task.TaskListActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private Button btnProfile, btnLogout, btnShop, btnEquipment;
    private Button btnCategories, btnAddTask, btnTaskList, btnTaskCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicijalizacija Firebase
        FirebaseApp.initializeApp(this);

        // Proveri da li je korisnik ulogovan i email verifikovan
        FirebaseUser currentUser = AuthManager.getCurrentUser(this);

        if (currentUser != null && currentUser.isEmailVerified()) {
            // Jedan layout koji ima sva dugmad
            setContentView(R.layout.activity_main);

            // Dugmad iz koleginicinog maina
            btnProfile = findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

            btnLogout = findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(v -> {
                AuthManager.logoutUser(this);
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });

            btnShop = findViewById(R.id.btnShop);
            btnShop.setOnClickListener(v -> {
                Intent intent = new Intent(this, ShopActivity.class);
                startActivity(intent);
            });

            btnEquipment = findViewById(R.id.btnEquipment);
            btnEquipment.setOnClickListener(v -> {
                Intent intent = new Intent(this, EquipmentActivity.class);
                startActivity(intent);
            });

            // Dugmad iz tvog maina
            btnCategories = findViewById(R.id.btnCategories);
            btnCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryActivity.class)));

            btnAddTask = findViewById(R.id.btnAddTask);
            btnAddTask.setOnClickListener(v -> startActivity(new Intent(this, AddTaskActivity.class)));

            btnTaskList = findViewById(R.id.btnTaskList);
            btnTaskList.setOnClickListener(v -> startActivity(new Intent(this, TaskListActivity.class)));

            btnTaskCalendar = findViewById(R.id.btnTaskCalendar);
            btnTaskCalendar.setOnClickListener(v -> startActivity(new Intent(this, TaskCalendarActivity.class)));

        } else {
            // Nije ulogovan → idi na LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
