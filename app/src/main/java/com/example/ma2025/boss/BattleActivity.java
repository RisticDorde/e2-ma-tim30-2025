package com.example.ma2025.boss;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.model.User;
import com.example.ma2025.repository.UserRepository;

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

    private int remainingAttacks = 5;
    private double successChance = 67.0; // za test

    private int currentBossHpMax;
    private int currentBossHp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        userRepository = new UserRepository(this);
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
        shakeDetector = new ShakeDetector(this::onChestShaken);


        attackButton.setOnClickListener(v -> doAttack());
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector(() -> doAttack());
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

        updateUI();
    }

    private void doAttack() {
        if (remainingAttacks <= 0) {
            Toast.makeText(this, "Nema više pokušaja!", Toast.LENGTH_SHORT).show();
            return;
        }

        remainingAttacks--;
        int random = new Random().nextInt(100);
        int userPP = currentUser.getPowerPoints();

        if (random < successChance) {
            currentBossHp = Math.max(0, currentBossHp - userPP);
            Toast.makeText(this, "Pogodak! -" + userPP + " HP", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        updateUI();

        if (currentBossHp <= 0) {
            showChest();
            endBattle(true);
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
        } else {
            currentUser.setBossRemainingHp(currentBossHp);
            Toast.makeText(this, "Boss preživeo! Pokušaćeš ponovo kasnije.", Toast.LENGTH_SHORT).show();
        }

        userRepository.updateUser(currentUser);
        finish();
    }

    private void showChest() {
        bossImage.setVisibility(View.GONE);
        chestImage.setVisibility(View.VISIBLE);
        chestHint.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Boss je poražen! Kovčeg se pojavio!", Toast.LENGTH_SHORT).show();
    }

    private void onChestShaken() {
        if (chestImage.getVisibility() == View.VISIBLE) {
            chestImage.setImageResource(R.drawable.chest_open);
            chestHint.setText("Kovčeg otvoren! Dobio si nagradu");

            // Nagrada — dodaj XP, coins, itd.
            //userRepository.addCoins(currentUser.getId(), 100);
            currentUser.addExperience(0, true, 100, userRepository);

            Toast.makeText(this, "Dobio si 100 novčića i 50 XP!", Toast.LENGTH_LONG).show();

            shakeDetector.stop(); // zaustavi dalje detekcije
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
