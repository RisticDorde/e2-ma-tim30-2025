package com.example.ma2025.task;

import android.os.Bundle;
import android.util.Log;
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
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaskDetailsActivity extends AppCompatActivity {

    private EditText etName, etDescription, etExecutionDate, etStartDate, etEndDate, inputTotalXp;
    private Button btnEdit, btnSave, btnDelete;
    private Spinner spinnerDifficulty, spinnerImportance, spinnerStatus;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
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
        inputTotalXp = findViewById(R.id.input_total_xp);

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

        userRepository = new UserRepository(this);

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
        inputTotalXp.setText(String.valueOf(currentTask.getTotalXP()));

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
        TaskStatus oldStatus = currentTask.getTaskStatus();
        TaskStatus newStatus = (TaskStatus) spinnerStatus.getSelectedItem();

        currentTask.setName(etName.getText().toString());
        currentTask.setDescription(etDescription.getText().toString());currentTask.setImportance((TaskImportance) spinnerImportance.getSelectedItem());
        currentTask.setDifficulty((TaskDifficulty) spinnerDifficulty.getSelectedItem());
        currentTask.setTaskStatus((TaskStatus) spinnerStatus.getSelectedItem());
        //

        taskRepository.updateTask(currentTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Zadatak ažuriran", Toast.LENGTH_SHORT).show();
                    enableEditing(false);

                    Log.d("TaskDetails", "oldStatus=" + oldStatus + " newStatus=" + newStatus);

                    if (oldStatus != TaskStatus.ACCOMPLISHED && newStatus == TaskStatus.ACCOMPLISHED) {
                        checkAndAwardXp(currentTask);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Greška pri čuvanju", Toast.LENGTH_SHORT).show());


        //taskRepository.updateTask(currentTask);
                //.addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Zadatak ažuriran", Toast.LENGTH_SHORT).show();
                    //enableEditing(false);
                //})
                //.addOnFailureListener(e -> Toast.makeText(this, "Greška pri čuvanju", Toast.LENGTH_SHORT).show());
    }
/*
    private void checkAndAwardXp(Task task) {
        int xp = task.getTotalXP();
        int limit;
        long timeLimitMs = 0L;

        // Odredi kvotu i vremenski period
        TaskDifficulty diff = task.getDifficulty();
        TaskImportance imp = task.getImportance();

        if ((diff == TaskDifficulty.VERY_EASY && imp == TaskImportance.NORMAL) ||
                (diff == TaskDifficulty.EASY && imp == TaskImportance.IMPORTANT)) {
            limit = 5;
            timeLimitMs = 24L * 60 * 60 * 1000; // dnevno
        } else if (diff == TaskDifficulty.HARD && imp == TaskImportance.EXTREME_IMPORTANT) {
            limit = 2;
            timeLimitMs = 24L * 60 * 60 * 1000; // dnevno
        } else if (diff == TaskDifficulty.EXTREME) {
            limit = 1;
            timeLimitMs = 7L * 24 * 60 * 60 * 1000; // nedeljno
        } else if (imp == TaskImportance.SPECIAL) {
            limit = 1;
            timeLimitMs = 30L * 24 * 60 * 60 * 1000; // mesečno
        } else {
            limit = 0;
            // Ako kombinacija nije definisana u pravilima, nema nagrade
            Toast.makeText(this, "Ova kombinacija težine i važnosti ne nosi XP.", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        long fromTime = now - timeLimitMs;

        // 🔍 Proveri koliko zadataka je korisnik već završio u tom periodu
        taskRepository.getAccomplishedTasksSince(fromTime)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();

                    if (count < limit) {
                        addXpToUser(xp);
                        Toast.makeText(this, "XP dodat! +" + xp, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Dostignut limit za ovaj tip zadatka.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Greška pri proveri limita XP-a.", Toast.LENGTH_SHORT).show()
                );
    }
*/
private void checkAndAwardXp(Task task) {
    if (task.getTaskStatus() != TaskStatus.ACCOMPLISHED) return; // samo kad postane accomplished

    int xp = task.getTotalXP();
    int limit;
    long timeLimitMs = 0L;

    TaskDifficulty diff = task.getDifficulty();
    TaskImportance imp = task.getImportance();

    //  Ograničenja po težini i važnosti zadatka
    if ((diff == TaskDifficulty.VERY_EASY || imp == TaskImportance.NORMAL) ||
            (diff == TaskDifficulty.EASY || imp == TaskImportance.IMPORTANT)) {
        limit = 5;
        timeLimitMs = 24L * 60 * 60 * 1000; // dnevno
    } else if (diff == TaskDifficulty.HARD || imp == TaskImportance.EXTREME_IMPORTANT) {
        limit = 2;
        timeLimitMs = 24L * 60 * 60 * 1000; // dnevno
    } else if (diff == TaskDifficulty.EXTREME) {
        limit = 1;
        timeLimitMs = 7L * 24 * 60 * 60 * 1000; // nedeljno
    } else if (imp == TaskImportance.SPECIAL) {
        limit = 1;
        timeLimitMs = 30L * 24 * 60 * 60 * 1000; // mesečno
    } else {
        Toast.makeText(this, "Ova kombinacija težine i važnosti ne nosi XP.", Toast.LENGTH_SHORT).show();
        return;
    }

    long now = System.currentTimeMillis();
    long fromTime = now - timeLimitMs;

    //  Dohvati trenutno prijavljenog korisnika iz lokalne baze
    User currentUser = userRepository.getCurrentAppUser(this);
    if (currentUser == null) {
        Toast.makeText(this, "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
        return;
    }

    //  Firestore: proveri koliko je već accomplished zadataka u periodu
    taskRepository.getAccomplishedTasksSince(fromTime)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int count = querySnapshot.size();

                Log.d("TaskDetails", "Count vrednost = " + count);
                Log.d("TaskDetails", "Limit vrednost = " + limit);


                if (count < limit) {
                    //  Koristi metodu iz User klase koja računa XP i nivo
                    // Ako je zadatak “boss fight”, stavi true i dodaj nagradu u coinsima
                    /*boolean isBossFight = task.isBossFight(); // ako nemaš, možeš dodati u Task model
                    int rewardCoins = isBossFight ? 100 : 0;

                    currentUser.addExperience(xp, isBossFight, rewardCoins, userRepository);
                    */
                    Log.d("TaskDetails", "**********************USAO U METODU!!!");
                    currentUser.addExperience(xp, false, 0, userRepository);
                    // Obavesti korisnika o napretku
                    Toast.makeText(this,
                            "XP dodat! +" + xp + "\nTrenutni nivo: " +
                                    currentUser.getLevelNumber() +
                                    " (" + currentUser.getExperiencePoints() + " XP)",
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Dostignut limit za ovaj tip zadatka.", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e ->
                    Toast.makeText(this, "Greška pri proveri limita XP-a.", Toast.LENGTH_SHORT).show()
            );
    }



    private void addXpToUser(int xp) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Nema prijavljenog korisnika.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);

            // trenutni XP (ako ne postoji, pretpostavi 0)
            Long currentXp = snapshot.getLong("xp");
            if (currentXp == null) currentXp = 0L;

            long newXp = currentXp + xp;

            transaction.update(userRef, "xp", newXp);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Dodato " + xp + " XP!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("TaskDetails", "Greška pri dodavanju XP-a", e);
            Toast.makeText(this, "Greška pri dodavanju XP-a: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

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
