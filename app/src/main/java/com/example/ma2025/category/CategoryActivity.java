package com.example.ma2025.category;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.example.ma2025.model.Category;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private CategoryRepository repository;

    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        repository = new CategoryRepository();

        recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(categories);
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

                        if (!name.isEmpty() && color.matches("^#([A-Fa-f0-9]{6})$")) {
                            Category newCat = new Category(null, name, color);
                            repository.addCategory(newCat);
                        }
                    })
                    .setNegativeButton("Otkaži", null)
                    .show();
        });


        // slušaj promene u Firestore
        listenerRegistration = repository.getAllCategories()
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

    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}