package com.example.ma2025.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ma2025.R;
import com.example.ma2025.model.Potion;
import com.example.ma2025.model.User;
import com.example.ma2025.model.Weapon;
import com.example.ma2025.model.Clothing;
import com.example.ma2025.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class EquipmentActivity extends AppCompatActivity {

    private RecyclerView rvActiveEquipment, rvAvailableEquipment;
    private List<Object> activeEquipmentList = new ArrayList<>();
    private List<Object> availableEquipmentList = new ArrayList<>();
    private ItemEquipmentActivity activeAdapter, availableAdapter;

    private User user;
    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        userRepo = new UserRepository(this);
        user = userRepo.getCurrentAppUser(this);

        setupRecyclerViews();
        loadEquipmentData();
    }

    private void setupRecyclerViews() {
        rvActiveEquipment = findViewById(R.id.rvActiveEquipment);
        rvAvailableEquipment = findViewById(R.id.rvAvailableEquipment);

        rvActiveEquipment.setLayoutManager(new LinearLayoutManager(this));
        rvAvailableEquipment.setLayoutManager(new LinearLayoutManager(this));

        activeAdapter = new ItemEquipmentActivity(activeEquipmentList, true);
        availableAdapter = new ItemEquipmentActivity(availableEquipmentList, false);

        rvActiveEquipment.setAdapter(activeAdapter);
        rvAvailableEquipment.setAdapter(availableAdapter);

        // Postavi listener za activate dugme
        availableAdapter.setOnActivateClickListener((equipment, position) -> {
            activateEquipment(equipment, position);
        });
    }

    private void activateEquipment(Object equipment, int position) {
        boolean success = false;

        if (equipment instanceof Potion) {
            Potion potion = (Potion) equipment;
            potion.setActivated(true);
            user.getPotions().set(user.getPotions().indexOf(potion), potion);
            success = true;
            Toast.makeText(this, potion.getName() + " activated!", Toast.LENGTH_SHORT).show();

        } else if (equipment instanceof Weapon) {
            Weapon weapon = (Weapon) equipment;
            // Dodaj u equipment listu
            if (!user.getEquipment().contains(weapon.getName())) {
                user.getEquipment().add(weapon.getName());
            }
            success = true;
            Toast.makeText(this, weapon.getName() + " equipped!", Toast.LENGTH_SHORT).show();

        } else if (equipment instanceof Clothing) {
            Clothing clothing = (Clothing) equipment;
            clothing.setActivated(true);
            user.getClothings().set(user.getClothings().indexOf(clothing), clothing);
            success = true;
            Toast.makeText(this, clothing.getName() + " activated!", Toast.LENGTH_SHORT).show();
        }

        if (success) {
            // Sačuvaj promene
            userRepo.updateUser(user);

            // Osvježi prikaz
            loadEquipmentData();
        }
    }

    private void loadEquipmentData() {
        activeEquipmentList.clear();
        availableEquipmentList.clear();

        for (Potion potion : user.getPotions()) {
            if (potion.isActivated() && !potion.isExpired()) {
                activeEquipmentList.add(potion);
            } else if (!potion.isExpired()) {
                availableEquipmentList.add(potion);
            }
        }

        for (Weapon weapon : user.getWeapons()) {
            if (weapon.isOwned() && user.getEquipment().contains(weapon.getName())) {
                activeEquipmentList.add(weapon);
            } else if (weapon.isOwned()) {
                availableEquipmentList.add(weapon);
            }
        }

        for (Clothing clothing : user.getClothings()) {
            if (clothing.isActivated() && !clothing.isExpired()) {
                activeEquipmentList.add(clothing);
            } else if (!clothing.isExpired()) {
                availableEquipmentList.add(clothing);
            }
        }

        activeAdapter.notifyDataSetChanged();
        availableAdapter.notifyDataSetChanged();
    }

    private void showWeaponUpgradeDialog(Weapon weapon) {
        // Izračunaj cenu unapređenja (60% od prethodnog bossa)
        long previousBossReward = computeCoinsForBossIndex(user.getCurrentBossIndex() - 1);
        int upgradePrice = (int) (previousBossReward * 0.60);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upgrade " + weapon.getName());
        builder.setMessage(
                "Current level: " + weapon.getUpgradeLevel() + "\n" +
                        "Current bonus: +" + (weapon.getBonus() * 100) + "%\n\n" +
                        "After upgrade:\n" +
                        "Level: " + (weapon.getUpgradeLevel() + 1) + "\n" +
                        "Bonus: +" + ((weapon.getBonus() + 0.01) * 100) + "%\n\n" +
                        "Price: " + upgradePrice + " coins"
        );

        builder.setPositiveButton("Upgrade", (dialog, which) -> {
            if (user.getCoins() >= upgradePrice) {
                // Oduzmi novčiće
                user.setCoins(user.getCoins() - upgradePrice);

                // Unapredi oružje
                weapon.upgrade();

                // Sačuvaj promene
                userRepo.updateUser(user);

                Toast.makeText(this,
                        weapon.getName() + " upgraded to level " + weapon.getUpgradeLevel() + "!",
                        Toast.LENGTH_LONG).show();

                // Osvježi prikaz
                loadEquipmentData();
            } else {
                Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private long computeCoinsForBossIndex(int index) {
        if (index <= 0) return 200;
        double coins = 200;
        for (int i = 1; i < index; i++) {
            coins *= 1.2;
        }
        return Math.round(coins);
    }
}