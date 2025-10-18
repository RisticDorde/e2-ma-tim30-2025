package com.example.ma2025.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.category.CategoryRepository;
import com.example.ma2025.model.Category;
import com.example.ma2025.model.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etName, etDescription, etInterval;
    private Spinner spinnerFrequency, spinnerIntervalUnit, spinnerDifficulty, spinnerImportance, spinnerCategory;
    private LinearLayout layoutOnce, layoutRepeating;
    private Button btnPickExecutionDate, btnPickStartDate, btnPickEndDate, btnSave;
    private TextView tvExecutionDate, tvStartDate, tvEndDate;

    private Date executionDate, startDate, endDate;

    private TaskRepository repository;
    private CategoryRepository categoryRepository;

    List<Category> categoryList = new ArrayList<>();
    List<String> categoryNames = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        repository = new TaskRepository();
        categoryRepository = new CategoryRepository();

        etName = findViewById(R.id.etTaskName);
        etDescription = findViewById(R.id.etTaskDescription);
        etInterval = findViewById(R.id.etInterval);

        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        spinnerIntervalUnit = findViewById(R.id.spinnerIntervalUnit);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        layoutOnce = findViewById(R.id.layoutOnce);
        layoutRepeating = findViewById(R.id.layoutRepeating);

        btnPickExecutionDate = findViewById(R.id.btnPickExecutionDate);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        btnSave = findViewById(R.id.btnSaveTask);

        tvExecutionDate = findViewById(R.id.tvExecutionDate);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);

        // Puni spinnere
        spinnerFrequency.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                TaskFrequency.values()));

        spinnerIntervalUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                TaskIntervalUnit.values()));

        spinnerImportance.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                TaskImportance.values()));

        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                TaskDifficulty.values()));

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categoryNames
        );
        spinnerCategory.setAdapter(categoryAdapter);

        categoryRepository.getAllCategories()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    categoryNames.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Category c = doc.toObject(Category.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            categoryList.add(c);
                            categoryNames.add(c.getName());
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                });

        // Promena frekvencije
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                TaskFrequency freq = (TaskFrequency) spinnerFrequency.getSelectedItem();
                if (freq == TaskFrequency.ONETIME) {
                    layoutOnce.setVisibility(View.VISIBLE);
                    layoutRepeating.setVisibility(View.GONE);
                } else {
                    layoutOnce.setVisibility(View.GONE);
                    layoutRepeating.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Date pickeri
        btnPickExecutionDate.setOnClickListener(v -> pickDate(date -> {
            executionDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            tvExecutionDate.setText(date.format(formatter)); // koristi LocalDate za prikaz
        }));

        btnPickStartDate.setOnClickListener(v -> pickDate(date -> {
            startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            tvStartDate.setText(date.format(formatter));
        }));

        btnPickEndDate.setOnClickListener(v -> pickDate(date -> {
            endDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            tvEndDate.setText(date.format(formatter));
        }));

        // Sačuvaj
        btnSave.setOnClickListener(v -> saveTask());
    }

    private interface DateCallback { void onDatePicked(LocalDate date); }

    private void pickDate(DateCallback callback) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
                    callback.onDatePicked(picked);
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveTask() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        TaskFrequency freq = (TaskFrequency) spinnerFrequency.getSelectedItem();
        TaskDifficulty dif = (TaskDifficulty) spinnerDifficulty.getSelectedItem();
        TaskImportance imp = (TaskImportance) spinnerImportance.getSelectedItem();
        int pos = spinnerCategory.getSelectedItemPosition();
        Category selected = categoryList.get(pos);
        String categoryId = selected.getId();


        if (name.isEmpty()) {
            Toast.makeText(this, "Unesite naziv zadatka", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task();
        task.setName(name);
        task.setDescription(desc);
        task.setFrequency(freq);
        task.setDifficulty(dif);
        task.setImportance(imp);
        task.setCategoryId(categoryId);
        task.setTotalXP( task.getDifficulty().getXp() + task.getImportance().getXp());
        task.setTaskStatus(TaskStatus.ACTIVE);

        if (freq == TaskFrequency.ONETIME) {
            if (executionDate == null) {
                Toast.makeText(this, "Odaberite datum izvršenja", Toast.LENGTH_SHORT).show();
                return;
            }
            task.setExecutionDate(executionDate);
        } else {
            // REPEATING
            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Odaberite početni i krajnji datum", Toast.LENGTH_SHORT).show();
                return;
            }
            String intervalStr = etInterval.getText().toString().trim();
            if (intervalStr.isEmpty()) {
                Toast.makeText(this, "Unesite interval", Toast.LENGTH_SHORT).show();
                return;
            }
            int interval = Integer.parseInt(intervalStr);
            TaskIntervalUnit unit = (TaskIntervalUnit) spinnerIntervalUnit.getSelectedItem();

            task.setStartDate(startDate);
            task.setEndDate(endDate);
            task.setInterval(interval);
            task.setIntervalUnit(unit);
        }

        repository.addTask(task);
        Toast.makeText(this, "Zadatak sačuvan", Toast.LENGTH_SHORT).show();
        finish();
    }
}
