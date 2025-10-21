package com.example.ma2025.boss;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;
import com.example.ma2025.task.TaskFrequency;
import com.example.ma2025.task.TaskRepository;
import com.example.ma2025.task.TaskStatus;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.Random;

public class BattleActivity extends AppCompatActivity {

    private ProgressBar bossHpBar, userPpBar;
    private TextView bossHpText, userPpText, attemptsText, chanceText, chestHint;
    private ImageView bossImage, equipmentImage, chestImage;
    private Button attackButton;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    private User currentUser;
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    private int remainingAttacks = 5;
    private double successChance = 67.0; // za test

    private int currentBossHpMax;
    private int currentBossHp;

    private boolean isAttackInProgress = false;

    private boolean isBattleEnded = false;
    private boolean isChestOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        userRepository = new UserRepository(this);
        taskRepository = new TaskRepository();

        currentUser = userRepository.getCurrentAppUser(this);

        if (currentUser == null) {
            Toast.makeText(this, "Nema aktivnog korisnika.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initSensors();
        setupBattle();
    }

    private void initViews() {
        bossHpBar = findViewById(R.id.bossHpBar);
        userPpBar = findViewById(R.id.userPpBar);
        bossHpText = findViewById(R.id.bossHpText);
        userPpText = findViewById(R.id.userPpText);
        attemptsText = findViewById(R.id.attemptsText);
        chanceText = findViewById(R.id.chanceText);
        bossImage = findViewById(R.id.bossImage);
        equipmentImage = findViewById(R.id.equipmentImage);
        attackButton = findViewById(R.id.attackButton);
        chestImage = findViewById(R.id.chestImage);
        chestHint = findViewById(R.id.chestHint);
        //shakeDetector = new ShakeDetector(this::onChestShaken);


        attackButton.setOnClickListener(v -> doAttack());
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeDetector = new ShakeDetector(() -> {
            if (chestImage.getVisibility() == View.VISIBLE && !isChestOpened) {
                onChestShaken(); // Ako je kovčeg vidljiv, otvori ga
            } else if (!isBattleEnded) {
                doAttack(); // Inače, napadaj
            }
        });
    }

    private void setupBattle() {
        int bossIndex = currentUser.getCurrentBossIndex();

        // ako je korisnik na nivou 1, još nema bossa
        if (bossIndex == 0) {
            Toast.makeText(this, "Nema dostupne borbe — pređi prvi nivo!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentBossHpMax = computeBossHpForIndex(bossIndex);

        // Ako ima preostali HP iz prethodne borbe, koristi njega
        if (currentUser.getBossRemainingHp() > 0 && currentUser.getBossRemainingHp() < currentBossHpMax) {
            currentBossHp = (int)currentUser.getBossRemainingHp();
        } else {
            currentBossHp = currentBossHpMax;
        }

        // Asinhrono
        calculateSuccessRate(successRate -> {
            this.successChance = successRate;
            updateUI(); // Ažuriraj UI nakon što dobiješ rezultat
        });

        //updateUI(); // Inicijalani prikaz
    }

    private void doAttack() {
        if (isBattleEnded) return;
        if (isAttackInProgress) return; // Blokiraj multiple napade

        if (remainingAttacks <= 0) {
            Toast.makeText(this, "Nema više pokušaja!", Toast.LENGTH_SHORT).show();
            return;
        }
        isAttackInProgress = true;

        remainingAttacks--;
        int random = new Random().nextInt(100);
        int userPP = currentUser.getPowerPoints();

        if (random < successChance) {
            currentBossHp = Math.max(0, currentBossHp - userPP);
            Toast.makeText(this, "Pogodak! -" + userPP + " HP", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isAttackInProgress = false;
        }, 1000); // 1 sekunda zaštite

        updateUI();

        if (currentBossHp <= 0) {
            isBattleEnded = true;
            showChest();
            // NE POZIVAJ endBattle(true) odmah!
            // Pozovi ga tek nakon otvaranja kovčega
        } else if (remainingAttacks == 0) {
            endBattle(false);
        }
    }

    private void updateUI() {
        bossHpBar.setMax(currentBossHpMax);
        bossHpBar.setProgress(currentBossHp);
        bossHpText.setText("Boss HP: " + currentBossHp + "/" + currentBossHpMax);

        int pp = currentUser.getPowerPoints();
        userPpBar.setMax(pp * 2);
        userPpBar.setProgress(pp);
        userPpText.setText("Tvoja snaga (PP): " + pp);

        attemptsText.setText("Preostali napadi: " + remainingAttacks + "/5");
        chanceText.setText("Šansa: " + (int) successChance + "%");
    }

    private void endBattle(boolean bossDefeated) {
        if (bossDefeated) {
            long coins = computeCoinsForBossIndex(currentUser.getCurrentBossIndex());
            userRepository.addCoins(currentUser.getId(), (int) coins);

            Toast.makeText(this, "Pobeda! Osvojeno " + coins + " novčića!", Toast.LENGTH_LONG).show();

            // prelazak na sledećeg bossa
            currentUser.setCurrentBossIndex(currentUser.getCurrentBossIndex() + 1);
            currentUser.setBossRemainingHp(0);
        }
        else {
            // Proveri 50% HP reduction
            double hpReduction = 1.0 - ((double) currentBossHp / currentBossHpMax);

            if (hpReduction >= 0.5) {
                long halfCoins = computeCoinsForBossIndex(currentUser.getCurrentBossIndex()) / 2;
                userRepository.addCoins(currentUser.getId(), (int) halfCoins);

                // 10% šansa za equipment (upola od 20%)
                if (new Random().nextInt(100) < 10) {
                    // TODO: Dodaj equipment
                }

                Toast.makeText(this, "Delimična pobeda! Osvojeno " + halfCoins + " novčića!",
                        Toast.LENGTH_LONG).show();
            } else {
                currentUser.setBossRemainingHp(currentBossHp);
                Toast.makeText(this, "Boss preživeo! Pokušaćeš ponovo kasnije.", Toast.LENGTH_SHORT).show();
            }

            userRepository.updateUser(currentUser);
            finish();
        }
    }

    private void showChest() {
        bossImage.setVisibility(View.GONE);
        chestImage.setVisibility(View.VISIBLE);
        chestHint.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Boss je poražen! Kovčeg se pojavio!", Toast.LENGTH_SHORT).show();
    }

    private void onChestShaken() {
        if (chestImage.getVisibility() == View.VISIBLE) {
            isChestOpened = true;
            chestImage.setImageResource(R.drawable.chest_open);
            chestHint.setText("Kovčeg otvoren! Dobio si nagradu");

            // Nagrada — dodaj XP, coins, itd.
            //userRepository.addCoins(currentUser.getId(), 100);
            //currentUser.addExperience(0, true, 100, userRepository);

            // Izračunaj stvarne nagrade
            long coins = computeCoinsForBossIndex(currentUser.getCurrentBossIndex());
            userRepository.addCoins(currentUser.getId(), (int) coins);

            currentUser.setLastLevelUpAt(String.valueOf(System.currentTimeMillis()));
            Log.d("BOSS_DEFEATED", "Boss defeated! Setting lastLevelUpAt = " + currentUser.getLastLevelUpAt());

            //Toast.makeText(this, "Dobio si 100 novčića i 50 XP!", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Dobio si " + coins + " novčića ",
                    Toast.LENGTH_LONG).show();

            // TODO: 20% šansa za equipment

            shakeDetector.stop(); // zaustavi dalje detekcije
            sensorManager.unregisterListener(shakeDetector);

            // Sada zatvori aktivnost nakon pauze
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                endBattle(true);
            }, 3000); // 3 sekunde da vidi nagrade
        }
    }



    // formula za HP bossa
    private int computeBossHpForIndex(int index) {
        if (index == 1) return 200;
        int hp = 200;
        for (int i = 2; i <= index; i++) {
            hp = hp * 2 + hp / 2;
        }
        return hp;
    }

    // formula za novčiće
    private long computeCoinsForBossIndex(int index) {
        double coins = 200;
        for (int i = 1; i < index; i++) coins *= 1.2;
        return Math.round(coins);
    }

//    private void calculateSuccessRate(SuccessRateCallback callback) {
//        String loggedUserId = AuthManager.getCurrentUser(this).getUid();
//        String lastLevelUp = currentUser.getLastLevelUpAt();
//        long fromTimestamp;
//
//        if (lastLevelUp == null || lastLevelUp.isEmpty() || lastLevelUp.equals("0")) {
//            // Ako je null, uzmi vreme kreiranja accounta ili 0
//            fromTimestamp = 0; // Sve zadatke od početka
//            // ILI: fromTimestamp = Long.parseLong(currentUser.getCreatedAt());
//        } else {
//            fromTimestamp = Long.parseLong(lastLevelUp);
//        }
//
//        // Prebrojavanje accomplished tasks
//        taskRepository.getUserAccomplishedTasksSince(loggedUserId, fromTimestamp)
//                .get()
//                .addOnSuccessListener(accomplishedSnapshot -> {
//                    int completedTasks = accomplishedSnapshot.size();
//
//                    // Prebrojavanje total valid tasks
//                    taskRepository.getUserTotalValidTasksSince(loggedUserId, fromTimestamp)
//                            .get()
//                            .addOnSuccessListener(totalSnapshot -> {
//                                // Filtriraj PAUSED taskove na klijentskoj strani (Firebase limit)
//                                int totalTasks = 0;
//                                for (DocumentSnapshot doc : totalSnapshot.getDocuments()) {
//                                    String status = doc.getString("taskStatus");
//                                    if (status != null && !status.equals(TaskStatus.PAUSED.name())) {
//                                        totalTasks++;
//                                    }
//                                }
//
//                                double successRate = totalTasks == 0 ? 0.0 : (completedTasks * 100.0) / totalTasks;
//                                callback.onCalculated(successRate);
//                            })
//                            .addOnFailureListener(e -> {
//                                Log.e("BattleActivity", "Error fetching total tasks", e);
//                                callback.onCalculated(0.0); // Default na error
//                            });
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("BattleActivity", "Error fetching completed tasks", e);
//                    callback.onCalculated(0.0);
//                });
//    }

//    private void calculateSuccessRate(SuccessRateCallback callback) {
//        String loggedUserId = AuthManager.getCurrentUser(this).getUid();
//        String lastLevelUp = currentUser.getLastLevelUpAt();
//        long fromTimestamp;
//
//        if (lastLevelUp == null || lastLevelUp.isEmpty() || lastLevelUp.equals("0")) {
//            fromTimestamp = 0;
//            Log.d("SUCCESS_RATE", "lastLevelUpAt je null/prazan - uzimam sve taskove (timestamp=0)");
//        } else {
//            fromTimestamp = Long.parseLong(lastLevelUp);
//            Log.d("SUCCESS_RATE", "lastLevelUpAt = " + lastLevelUp + " (" + new java.util.Date(fromTimestamp) + ")");
//        }
//
//        Log.d("SUCCESS_RATE", "============================================");
//        Log.d("SUCCESS_RATE", "Račuam success rate za userId: " + loggedUserId);
//        Log.d("SUCCESS_RATE", "Od timestampa: " + fromTimestamp);
//        Log.d("SUCCESS_RATE", "============================================");
//
//        // Prebrojavanje accomplished tasks
//        taskRepository.getUserAccomplishedTasksSince(loggedUserId, fromTimestamp)
//                .get()
//                .addOnSuccessListener(accomplishedSnapshot -> {
//                    int completedTasks = accomplishedSnapshot.size();
//
//                    Log.d("SUCCESS_RATE", "--- ACCOMPLISHED TASKS ---");
//                    Log.d("SUCCESS_RATE", "Broj accomplished taskova: " + completedTasks);
//
//                    // Detaljni log svakog accomplished taska
//                    for (DocumentSnapshot doc : accomplishedSnapshot.getDocuments()) {
//                        String title = doc.getString("title");
//                        String status = doc.getString("taskStatus");
//                        Long executionDate = doc.getLong("executionDate");
//                        //Long completedAt = doc.getLong("completedAt");
//
//                        Log.d("SUCCESS_RATE", "  ✓ Task: " + title);
//                        Log.d("SUCCESS_RATE", "    - Status: " + status);
//                        Log.d("SUCCESS_RATE", "    - Executed: " + (executionDate != null ? new java.util.Date(executionDate) : "null"));
//                        //Log.d("SUCCESS_RATE", "    - Completed: " + (completedAt != null ? new java.util.Date(completedAt) : "null"));
//                    }
//
//                    // Prebrojavanje total valid tasks
//                    taskRepository.getUserTotalValidTasksSince(loggedUserId, fromTimestamp)
//                            .get()
//                            .addOnSuccessListener(totalSnapshot -> {
//                                Log.d("SUCCESS_RATE", "--- TOTAL TASKS (pre filtriranja) ---");
//                                Log.d("SUCCESS_RATE", "Ukupno taskova iz querya: " + totalSnapshot.size());
//
//                                int totalTasks = 0;
//                                int pausedCount = 0;
//                                int canceledCount = 0; // ← DODAJ
//
//                                for (DocumentSnapshot doc : totalSnapshot.getDocuments()) {
//                                    String title = doc.getString("title");
//                                    String status = doc.getString("taskStatus");
//                                    Long executionDate = doc.getLong("executionDate");
//
//                                    Log.d("SUCCESS_RATE", "  Task: " + title);
//                                    Log.d("SUCCESS_RATE", "    - Status: " + status);
//                                    Log.d("SUCCESS_RATE", "    - Created: " + (executionDate != null ? new Date(executionDate) : "null"));
//
//                                    // Filtriraj PAUSED i CANCELED! ← PROMENA
//                                    if (status != null &&
//                                            !status.equals(TaskStatus.PAUSED.name()) &&
//                                            !status.equals(TaskStatus.CANCELED.name())) {
//                                        totalTasks++;
//                                        Log.d("SUCCESS_RATE", "    ✓ Računa se");
//                                    } else {
//                                        if (status != null && status.equals(TaskStatus.PAUSED.name())) {
//                                            pausedCount++;
//                                            Log.d("SUCCESS_RATE", "    ✗ PAUSED - ne računa se");
//                                        }
//                                        if (status != null && status.equals(TaskStatus.CANCELED.name())) {
//                                            canceledCount++;
//                                            Log.d("SUCCESS_RATE", "    ✗ CANCELED - ne računa se");
//                                        }
//                                    }
//                                }
//
//                                Log.d("SUCCESS_RATE", "--- REZULTAT ---");
//                                Log.d("SUCCESS_RATE", "Total taskova (posle filtriranja): " + totalTasks);
//                                Log.d("SUCCESS_RATE", "Paused taskova: " + pausedCount);
//                                Log.d("SUCCESS_RATE", "Canceled taskova: " + canceledCount);
//                                Log.d("SUCCESS_RATE", "Completed taskova: " + completedTasks);
//
//                                double successRate = totalTasks == 0 ? 0.0 : (completedTasks * 100.0) / totalTasks;
//
//                                Log.d("SUCCESS_RATE", "Success Rate = " + completedTasks + " / " + totalTasks + " = " + successRate + "%");
//
//                                callback.onCalculated(successRate);
//                            })
//                            .addOnFailureListener(e -> {
//                                Log.e("SUCCESS_RATE", "❌ ERROR fetching total tasks", e);
//                                Log.e("SUCCESS_RATE", "Error message: " + e.getMessage());
//                                callback.onCalculated(0.0);
//                            });
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("SUCCESS_RATE", "❌ ERROR fetching completed tasks", e);
//                    Log.e("SUCCESS_RATE", "Error message: " + e.getMessage());
//                    callback.onCalculated(0.0);
//                });
//    }

    private void calculateSuccessRate(SuccessRateCallback callback) {
        String loggedUserId = AuthManager.getCurrentUser(this).getUid();
        String lastLevelUp = currentUser.getLastLevelUpAt();
        long fromTimestamp;

        if (lastLevelUp == null || lastLevelUp.isEmpty() || lastLevelUp.equals("0")) {
            fromTimestamp = 0;
            Log.d("SUCCESS_RATE", "lastLevelUpAt je null/prazan - uzimam sve taskove (timestamp=0)");
        } else {
            fromTimestamp = Long.parseLong(lastLevelUp);
            Log.d("SUCCESS_RATE", "lastLevelUpAt = " + lastLevelUp + " (" + new Date(fromTimestamp) + ")");
        }

        Log.d("SUCCESS_RATE", "============================================");
        Log.d("SUCCESS_RATE", "Računam success rate za userId: " + loggedUserId);
        Log.d("SUCCESS_RATE", "Od timestampa: " + fromTimestamp + " (" + new Date(fromTimestamp) + ")");
        Log.d("SUCCESS_RATE", "============================================");

        final long finalFromTimestamp = fromTimestamp;

        taskRepository.getAllTasksByUserId(loggedUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("SUCCESS_RATE", "Ukupno taskova za usera: " + snapshot.size());

                    int completedTasks = 0;
                    int totalTasks = 0;
                    int pausedCount = 0;
                    int canceledCount = 0;
                    int oldTasksCount = 0;
                    int wrongFrequencyCount = 0;
                    int nullExecutionDateCount = 0;

                    Log.d("SUCCESS_RATE", "--- ANALIZA TASKOVA ---");

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String title = doc.getString("title");
                        String status = doc.getString("taskStatus");
                        String frequency = doc.getString("frequency");

                        // ✅ FIX: Konvertuj Timestamp u Long milliseconds
                        Long executionDateMillis = null;
                        try {
                            com.google.firebase.Timestamp executionDateTimestamp = doc.getTimestamp("executionDate");
                            if (executionDateTimestamp != null) {
                                executionDateMillis = executionDateTimestamp.toDate().getTime();
                            }
                        } catch (Exception e) {
                            // Možda je već Long? Pokušaj i to
                            executionDateMillis = doc.getLong("executionDate");
                        }

                        Log.d("SUCCESS_RATE", "\nTask: " + title);
                        Log.d("SUCCESS_RATE", "  - Status: " + status);
                        Log.d("SUCCESS_RATE", "  - Frequency: " + frequency);
                        Log.d("SUCCESS_RATE", "  - ExecutionDate: " +
                                (executionDateMillis != null ? executionDateMillis + " (" + new Date(executionDateMillis) + ")" : "NULL"));

                        // Filter 1: Proveri frequency
//                        if (frequency == null || !frequency.equals(TaskFrequency.ONETIME.name())) {
//                            wrongFrequencyCount++;
//                            Log.d("SUCCESS_RATE", "  ✗ Preskačem - nije ONETIME");
//                            continue;
//                        }
                        if (frequency == null || !frequency.trim().equalsIgnoreCase(TaskFrequency.ONETIME.name())) {
                            wrongFrequencyCount++;
                            Log.d("SUCCESS_RATE", "  ✗ Preskačem - nije ONETIME (" + frequency + ")");
                            continue;
                        }


                        // Filter 2: Proveri executionDate
                        if (executionDateMillis == null) {
                            nullExecutionDateCount++;
                            Log.d("SUCCESS_RATE", "  ✗ Preskačem - executionDate je NULL");
                            continue;
                        }

                        // Filter 3: Proveri da li je task u vremenskom periodu
                        if (executionDateMillis < finalFromTimestamp) {
                            oldTasksCount++;
                            Log.d("SUCCESS_RATE", "  ✗ Preskačem - stariji od fromTimestamp");
                            Log.d("SUCCESS_RATE", "    (" + executionDateMillis + " < " + finalFromTimestamp + ")");
                            continue;
                        }

                        // Filter 4: Proveri status
                        if (status == null) {
                            Log.d("SUCCESS_RATE", "  ✗ Preskačem - status je NULL");
                            continue;
                        }

                        if (status.equals(TaskStatus.CANCELED.name())) {
                            canceledCount++;
                            Log.d("SUCCESS_RATE", "  ✗ CANCELED - ne računa se");
                            continue;
                        }

                        if (status.equals(TaskStatus.PAUSED.name())) {
                            pausedCount++;
                            Log.d("SUCCESS_RATE", "  ✗ PAUSED - ne računa se");
                            continue;
                        }

                        // Task je validan za total count
                        totalTasks++;
                        Log.d("SUCCESS_RATE", "  ✓ Računa se u TOTAL");

                        // Proveri da li je ACCOMPLISHED
                        if (status.equals(TaskStatus.ACCOMPLISHED.name())) {
                            completedTasks++;
                            Log.d("SUCCESS_RATE", "  ✓✓ ACCOMPLISHED - računa se u completed!");
                        } else {
                            Log.d("SUCCESS_RATE", "  - Status: " + status + " (nije accomplished)");
                        }
                    }

                    Log.d("SUCCESS_RATE", "\n=== FINALNA STATISTIKA ===");
                    Log.d("SUCCESS_RATE", "Ukupno taskova u bazi: " + snapshot.size());
                    Log.d("SUCCESS_RATE", "Preskočeno (nisu ONETIME): " + wrongFrequencyCount);
                    Log.d("SUCCESS_RATE", "Preskočeno (executionDate NULL): " + nullExecutionDateCount);
                    Log.d("SUCCESS_RATE", "Preskočeno (stariji od timestamp): " + oldTasksCount);
                    Log.d("SUCCESS_RATE", "Preskočeno (PAUSED): " + pausedCount);
                    Log.d("SUCCESS_RATE", "Preskočeno (CANCELED): " + canceledCount);
                    Log.d("SUCCESS_RATE", "---");
                    Log.d("SUCCESS_RATE", "VALIDNI taskovi (totalTasks): " + totalTasks);
                    Log.d("SUCCESS_RATE", "ACCOMPLISHED taskovi: " + completedTasks);

                    double successRate = totalTasks == 0 ? 0.0 : (completedTasks * 100.0) / totalTasks;

                    Log.d("SUCCESS_RATE", "\n Success Rate = " + completedTasks + " / " + totalTasks + " = " + String.format("%.2f", successRate) + "%");
                    Log.d("SUCCESS_RATE", "============================================\n");

                    callback.onCalculated(successRate);
                })
                .addOnFailureListener(e -> {
                    Log.e("SUCCESS_RATE", " ERROR fetching tasks", e);
                    Log.e("SUCCESS_RATE", "Error message: " + e.getMessage());
                    callback.onCalculated(0.0);
                });
    }
    // Callback interface
    interface SuccessRateCallback {
        void onCalculated(double successRate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }
}
