package com.example.ma2025.task;

import android.app.DatePickerDialog;
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
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.model.Task;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    private EditText etName, etDescription, etExecutionDate, etStartDate, etEndDate, inputTotalXp;
    private Button btnEdit, btnSave, btnDelete;
    private Spinner spinnerDifficulty, spinnerImportance, spinnerStatus;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private String taskId;
    private Task currentTask;
    private boolean isEditing = false;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

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

        setupDatePickers();

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

    private void setupDatePickers() {
        etExecutionDate.setOnClickListener(v -> showDatePicker((date) -> {
            currentTask.setExecutionDate(date);
            etExecutionDate.setText(dateFormat.format(date));
        }));

        etStartDate.setOnClickListener(v -> showDatePicker((date) -> {
            currentTask.setStartDate(date);
            etStartDate.setText(dateFormat.format(date));
        }));

        etEndDate.setOnClickListener(v -> showDatePicker((date) -> {
            currentTask.setEndDate(date);
            etEndDate.setText(dateFormat.format(date));
        }));
    }

    private void showDatePicker(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    listener.onDateSelected(selected.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Helper interface
    interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    private void populateFields() {
        etName.setText(currentTask.getName());
        etDescription.setText(currentTask.getDescription());

        //etExecutionDate.setText(currentTask.getExecutionDate() != null ? currentTask.getExecutionDate().toString() : "-");
        //etStartDate.setText(currentTask.getStartDate() != null ? currentTask.getStartDate().toString() : "-");
        //etEndDate.setText(currentTask.getEndDate() != null ? currentTask.getEndDate().toString() : "-");

        etExecutionDate.setText(currentTask.getExecutionDate() != null
                ? dateFormat.format(currentTask.getExecutionDate())
                : "");
        etStartDate.setText(currentTask.getStartDate() != null
                ? dateFormat.format(currentTask.getStartDate())
                : "");
        etEndDate.setText(currentTask.getEndDate() != null
                ? dateFormat.format(currentTask.getEndDate())
                : "");


        inputTotalXp.setText(String.valueOf(currentTask.getTotalXP()));

        setSpinnerEnumSelection(spinnerImportance, currentTask.getImportance());
        setSpinnerEnumSelection(spinnerDifficulty, currentTask.getDifficulty());
        setSpinnerEnumSelection(spinnerStatus, currentTask.getTaskStatus());

        applyBusinessRules();
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
        //etExecutionDate.setEnabled(enable);
        //etStartDate.setEnabled(enable);
        if (currentTask.getFrequency() == TaskFrequency.ONETIME){
            etEndDate.setEnabled(false);
            etStartDate.setEnabled(false);
        }
        if (currentTask.getFrequency() == TaskFrequency.REPETITIVE){
            etExecutionDate.setEnabled(false);
        }
        //etEndDate.setEnabled(enable);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void saveChanges() {
        TaskStatus oldStatus = currentTask.getTaskStatus();
        TaskStatus newStatus = (TaskStatus) spinnerStatus.getSelectedItem();

        if (newStatus == TaskStatus.ACCOMPLISHED && isTaskInFuture()) {
            Toast.makeText(this, "Ne možete završiti zadatak koji je u budućnosti!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ako je PAUSED i nije repetitive, zabrani
        if (newStatus == TaskStatus.PAUSED && currentTask.getFrequency() != TaskFrequency.REPETITIVE) {
            Toast.makeText(this, "Samo ponavljajući zadaci mogu biti pauzirani!", Toast.LENGTH_SHORT).show();
            return;
        }

        //ako nije uneseno kroz date picker
        try {
            String execDateStr = etExecutionDate.getText().toString().trim();
            if (!execDateStr.isEmpty() && !execDateStr.equals("-")) {
                currentTask.setExecutionDate(dateFormat.parse(execDateStr));
            }

            String startDateStr = etStartDate.getText().toString().trim();
            if (!startDateStr.isEmpty() && !startDateStr.equals("-")) {
                currentTask.setStartDate(dateFormat.parse(startDateStr));
            }

            String endDateStr = etEndDate.getText().toString().trim();
            if (!endDateStr.isEmpty() && !endDateStr.equals("-")) {
                currentTask.setEndDate(dateFormat.parse(endDateStr));
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Neispravan format datuma! Koristite dd.MM.yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        currentTask.setName(etName.getText().toString());
        currentTask.setDescription(etDescription.getText().toString());currentTask.setImportance((TaskImportance) spinnerImportance.getSelectedItem());
        currentTask.setDifficulty((TaskDifficulty) spinnerDifficulty.getSelectedItem());
        currentTask.setTaskStatus((TaskStatus) spinnerStatus.getSelectedItem());
        //

        taskRepository.updateTask(currentTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Zadatak ažuriran", Toast.LENGTH_SHORT).show();
                    enableEditing(false);

                    if (newStatus == TaskStatus.PAUSED && currentTask.getFrequency() == TaskFrequency.REPETITIVE) {
                        pauseFutureChildTasks();
                    }

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

    String loggedUserId = AuthManager.getCurrentUser(this).getUid();

    //  Firestore: proveri koliko je već accomplished zadataka u periodu
    taskRepository.getUserAccomplishedTasksSince(loggedUserId, fromTime)
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

    private void pauseFutureChildTasks() {
        long now = System.currentTimeMillis();

        taskRepository.getTasksByParentId(currentTask.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        Task childTask = doc.toObject(Task.class);
                        childTask.setId(doc.getId());

                        // Pausiraj samo ako je u budućnosti
                        if (childTask.getExecutionDate() != null &&
                                childTask.getExecutionDate().getTime() > now) {

                            childTask.setTaskStatus(TaskStatus.PAUSED);
                            taskRepository.updateTask(childTask);
                        }
                    }
                    Toast.makeText(this, "Budući repetitivni zadaci su pauzirani", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyBusinessRules() {
        TaskStatus status = currentTask.getTaskStatus();
        //boolean isExpired = isTaskExpired();
        boolean canEdit = canStillEdit();

        boolean isFuture = isTaskInFuture();
        boolean isRepetitive = currentTask.getFrequency() == TaskFrequency.REPETITIVE;

        // Onemoguci Edit i Delete za vremenske završene i accomplished
        if (!canEdit || status == TaskStatus.ACCOMPLISHED) {
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
            btnEdit.setAlpha(0.5f);
            btnDelete.setAlpha(0.5f);
        }

        // Onemogući Edit za UNACCOMPLISHED
        if (status == TaskStatus.UNACCOMPLISHED || status == TaskStatus.CANCELED) {
            btnEdit.setEnabled(false);
            btnEdit.setAlpha(0.5f);
        }

        //Zadatak u budućnosti može samo da se otkaže
        if (isFuture && status == TaskStatus.ACTIVE) {
            btnEdit.setEnabled(true);
            btnEdit.setAlpha(1.0f);
            // Omogući samo prebacivanje u CANCELLED
            spinnerStatus.setEnabled(true);
            // Ostala polja onemogući
            //etName.setEnabled(false);
            //etDescription.setEnabled(false);
            //spinnerImportance.setEnabled(false);
            //spinnerDifficulty.setEnabled(false);
        }

        //PAUSED je dostupan samo za repetitive taskove
        if (!isRepetitive) {
            // Ukloni PAUSED iz spinner opcija
            ArrayAdapter<TaskStatus> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    getStatusOptionsWithoutPaused()
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(adapter);
            setSpinnerEnumSelection(spinnerStatus, currentTask.getTaskStatus());
        }
    }

    private TaskStatus[] getStatusOptionsWithoutPaused() {
        return new TaskStatus[]{
                TaskStatus.ACTIVE,
                TaskStatus.ACCOMPLISHED,
                TaskStatus.UNACCOMPLISHED,
                TaskStatus.CANCELED
        };
    }

    private boolean isTaskInFuture() {
        if (currentTask.getFrequency() == TaskFrequency.ONETIME && currentTask.getExecutionDate() == null) return false;
        if (currentTask.getFrequency() == TaskFrequency.REPETITIVE && currentTask.getEndDate() == null) return false;
        if(currentTask.getFrequency() == TaskFrequency.ONETIME)
            return currentTask.getExecutionDate().getTime() > System.currentTimeMillis();
        else
            return currentTask.getEndDate().getTime() > System.currentTimeMillis();
    }

    private boolean isTaskToday() {
        if (currentTask.getFrequency() == TaskFrequency.ONETIME && currentTask.getExecutionDate() == null) return false;
        if (currentTask.getFrequency() == TaskFrequency.REPETITIVE && currentTask.getEndDate() == null) return false;

        Calendar taskCal = Calendar.getInstance();
        if (currentTask.getFrequency() == TaskFrequency.ONETIME) {
            taskCal.setTime(currentTask.getExecutionDate());
        } else {
            taskCal.setTime(currentTask.getEndDate());
        }

        Calendar today = Calendar.getInstance();

        return taskCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                taskCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

//    private boolean isTaskExpired() {
//        if (currentTask.getFrequency() == TaskFrequency.ONETIME && currentTask.getExecutionDate() == null) return false;
//        if (currentTask.getFrequency() == TaskFrequency.REPETITIVE && currentTask.getEndDate() == null) return false;
//
//        Calendar taskCal = Calendar.getInstance();
//
//        if (currentTask.getFrequency() == TaskFrequency.ONETIME) {
//            taskCal.setTime(currentTask.getExecutionDate());
//        } else {
//            taskCal.setTime(currentTask.getEndDate());
//        }
//
//        taskCal.set(Calendar.HOUR_OF_DAY, 23);
//        taskCal.set(Calendar.MINUTE, 59);
//        taskCal.set(Calendar.SECOND, 59);
//
//        return taskCal.getTimeInMillis() < System.currentTimeMillis();
//    }

    // ZAMENI staru isTaskExpired() metodu sa ovom:
    private boolean isTaskExpired() {
        Date referenceDate;

        // Za repetitive taskove koristi endDate, za ostale executionDate
        if (currentTask.getFrequency() == TaskFrequency.REPETITIVE) {
            referenceDate = currentTask.getEndDate();
        } else {
            referenceDate = currentTask.getExecutionDate();
        }

        if (referenceDate == null) return false;

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(referenceDate);
        taskCal.set(Calendar.HOUR_OF_DAY, 23);
        taskCal.set(Calendar.MINUTE, 59);
        taskCal.set(Calendar.SECOND, 59);

        taskCal.add(Calendar.DAY_OF_YEAR, 3);

        return taskCal.getTimeInMillis() < System.currentTimeMillis();
    }

    // DODAJ novu helper metodu:
    private boolean canStillEdit() {
        Date referenceDate;

        if (currentTask.getFrequency() == TaskFrequency.REPETITIVE) {
            referenceDate = currentTask.getEndDate();
        } else {
            referenceDate = currentTask.getExecutionDate();
        }

        if (referenceDate == null) return true; // Ako nema datum, može se editovati

        Calendar gracePeriodEnd = Calendar.getInstance();
        gracePeriodEnd.setTime(referenceDate);
        gracePeriodEnd.set(Calendar.HOUR_OF_DAY, 23);
        gracePeriodEnd.set(Calendar.MINUTE, 59);
        gracePeriodEnd.set(Calendar.SECOND, 59);
        gracePeriodEnd.add(Calendar.DAY_OF_YEAR, 3); // 3 dana period

        return System.currentTimeMillis() <= gracePeriodEnd.getTimeInMillis();
    }
}
