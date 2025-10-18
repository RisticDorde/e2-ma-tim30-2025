package com.example.ma2025.task;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.model.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class TaskDetailsActivity extends AppCompatActivity {

    private EditText etName, etDescription, etExecutionDate, etStartDate, etEndDate;
    private Button btnEdit, btnSave, btnDelete;
    private Spinner spinnerDifficulty, spinnerImportance, spinnerStatus;

    private TaskRepository taskRepository;
    private String taskId;
    private Task currentTask;
    private boolean isEditing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etExecutionDate = findViewById(R.id.etExecutionDate);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);

        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        spinnerImportance = findViewById(R.id.spinnerImportance);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        spinnerImportance.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TaskImportance.values()
        ));
        ((ArrayAdapter<?>) spinnerImportance.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDifficulty.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TaskDifficulty.values()
        ));
        ((ArrayAdapter<?>) spinnerDifficulty.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerStatus.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TaskStatus.values()
        ));
        ((ArrayAdapter<?>) spinnerStatus.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        taskRepository = new TaskRepository();
        taskId = getIntent().getStringExtra("taskId");

        loadTaskDetails();

        btnEdit.setOnClickListener(v -> enableEditing(true));
        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> deleteTask());
    }

    private void loadTaskDetails() {
        taskRepository.getTaskById(taskId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    currentTask = doc.toObject(Task.class);
                    if (currentTask == null) {
                        Toast.makeText(this, "Greška: Zadatak ne postoji", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    currentTask.setId(doc.getId());
                    populateFields();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Greška pri učitavanju zadatka", Toast.LENGTH_SHORT).show());
    }

    private void populateFields() {
        etName.setText(currentTask.getName());
        etDescription.setText(currentTask.getDescription());
        etExecutionDate.setText(currentTask.getExecutionDate() != null ? currentTask.getExecutionDate().toString() : "-");
        etStartDate.setText(currentTask.getStartDate() != null ? currentTask.getStartDate().toString() : "-");
        etEndDate.setText(currentTask.getEndDate() != null ? currentTask.getEndDate().toString() : "-");

        setSpinnerEnumSelection(spinnerImportance, currentTask.getImportance());
        setSpinnerEnumSelection(spinnerDifficulty, currentTask.getDifficulty());
        setSpinnerEnumSelection(spinnerStatus, currentTask.getTaskStatus());
    }

    private <T extends Enum<T>> void setSpinnerEnumSelection(Spinner spinner, T value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void enableEditing(boolean enable) {
        isEditing = enable;
        etName.setEnabled(enable);
        etDescription.setEnabled(enable);
        etExecutionDate.setEnabled(enable);
        etStartDate.setEnabled(enable);
        etEndDate.setEnabled(enable);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void saveChanges() {
        currentTask.setName(etName.getText().toString());
        currentTask.setDescription(etDescription.getText().toString());currentTask.setImportance((TaskImportance) spinnerImportance.getSelectedItem());
        currentTask.setDifficulty((TaskDifficulty) spinnerDifficulty.getSelectedItem());
        currentTask.setTaskStatus((TaskStatus) spinnerStatus.getSelectedItem());
        // možeš dodati parsiranje datuma ako ih korisnik menja

        taskRepository.updateTask(currentTask);
                //.addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Zadatak ažuriran", Toast.LENGTH_SHORT).show();
                    //enableEditing(false);
                //})
                //.addOnFailureListener(e -> Toast.makeText(this, "Greška pri čuvanju", Toast.LENGTH_SHORT).show());
    }

    private void deleteTask() {
        taskRepository.deleteTask(currentTask);
                //.addOnSuccessListener(aVoid -> {
                   // Toast.makeText(this, "Zadatak obrisan", Toast.LENGTH_SHORT).show();
                   // finish();
                //})
                //.addOnFailureListener(e -> Toast.makeText(this, "Greška pri brisanju", Toast.LENGTH_SHORT).show());
    }
}
