package com.example.ma2025.activity;

import android.os.Bundle;
import android.widget.Toast;
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

        // Postavite layout managere
        rvActiveEquipment.setLayoutManager(new LinearLayoutManager(this));
        rvAvailableEquipment.setLayoutManager(new LinearLayoutManager(this));

        // Postavite adaptere (za sada prazne liste)
        rvActiveEquipment.setAdapter(new ItemEquipmentActivity(activeEquipmentList, true));
        rvAvailableEquipment.setAdapter(new ItemEquipmentActivity(availableEquipmentList, false));
    }

    private void loadEquipmentData() {
        // OČISTITE LISTE
        activeEquipmentList.clear();
        availableEquipmentList.clear();

        // DODAJTE POTIONE
        for (Potion potion : user.getPotions()) {
            if (potion.isActivated() && !potion.isExpired()) {
                activeEquipmentList.add(potion);
            } else {
                availableEquipmentList.add(potion);
            }
        }

        // DODAJTE ORUŽJE
        for (Weapon weapon : user.getWeapons()) {
            if (weapon.isOwned() && user.getEquipment().contains(weapon.getName())) {
                activeEquipmentList.add(weapon);
            } else if (weapon.isOwned()) {
                availableEquipmentList.add(weapon);
            }
        }

        // DODAJTE ODJEĆU
        for (Clothing clothing : user.getClothings()) {
            if (clothing.isActivated() && !clothing.isExpired()) {
                activeEquipmentList.add(clothing);
            } else {
                availableEquipmentList.add(clothing);
            }
        }

        // OBAVIJESTITE ADAPTER-E
        rvActiveEquipment.getAdapter().notifyDataSetChanged();
        rvAvailableEquipment.getAdapter().notifyDataSetChanged();
    }
}