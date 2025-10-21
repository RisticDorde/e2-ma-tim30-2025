package com.example.ma2025.category;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.model.Category;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private CategoryRepository repository;
    private UserRepository userRepository;
    private String currentUserId;
    private ListenerRegistration listenerRegistration;

    private List<String> usedColors = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        currentUserId = AuthManager.getCurrentUser(this).getUid();
        repository = new CategoryRepository();
        refreshUsedColors();

        recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(categories, category -> {
            // Klik na item -> otvori dijalog sa popunjenim vrednostima
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
            EditText etName = dialogView.findViewById(R.id.etCategoryName);
            EditText etColor = dialogView.findViewById(R.id.etCategoryColor);

            etName.setText(category.getName());
            etColor.setText(category.getColor());

            new AlertDialog.Builder(this)
                    .setTitle("Izmeni kategoriju")
                    .setView(dialogView)
                    .setPositiveButton("Sačuvaj", (dialog, which) -> {
                        String newName = etName.getText().toString().trim();
                        String newColor = etColor.getText().toString().trim();

                        if (newName.isEmpty()) {
                            Toast.makeText(this, "Naziv ne može biti prazan", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!newColor.matches("^#([A-Fa-f0-9]{6})$")) {
                            Toast.makeText(this, "Boja nije validna (#RRGGBB)", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!newColor.equals(category.getColor()) && usedColors.contains(newColor)) {
                            Toast.makeText(this, "Boja je već zauzeta", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        category.setName(newName);
                        category.setColor(newColor);
                        repository.updateCategory(category);
                        refreshUsedColors();
                    })
                    .setNegativeButton("Otkaži", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAddCategory);
        btnAdd.setOnClickListener(v -> {
            // otvara se dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
            EditText etName = dialogView.findViewById(R.id.etCategoryName);
            EditText etColor = dialogView.findViewById(R.id.etCategoryColor);

            new AlertDialog.Builder(this)
                    .setTitle("Nova kategorija")
                    .setView(dialogView)
                    .setPositiveButton("Sačuvaj", (dialog, which) -> {
                        String name = etName.getText().toString().trim();
                        String color = etColor.getText().toString().trim();

                        if(name.isEmpty()) {
                            Toast.makeText(this, "Unesi naziv kategorije", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(usedColors.contains(color)) {
                            Toast.makeText(this, "Boja je već zauzeta", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!name.isEmpty() && color.matches("^#([A-Fa-f0-9]{6})$")) {
                            Category newCat = new Category(null, name, color, currentUserId);
                            repository.addCategory(newCat);
                            refreshUsedColors();
                        }
                    })
                    .setNegativeButton("Otkaži", null)
                    .show();
        });


        // slušaj promene u Firestore
        listenerRegistration = repository.getCategoriesByUserId(currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    List<Category> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Category c = doc.toObject(Category.class);
                        list.add(c);
                    }
                    adapter.setCategories(list);
                });


    }

    private void refreshUsedColors() {
        repository.getCategoriesByUserId(currentUserId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> colors = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category c = doc.toObject(Category.class);
                if (c.getColor() != null) {
                    colors.add(c.getColor());
                }
            }
            usedColors = colors;
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}