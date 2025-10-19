package com.example.ma2025.model;

import java.util.ArrayList;
import java.util.List;

public class Shop {
    private List<Potion> potions;
    private List<Clothing> clothes;

    public Shop() {
        potions = new ArrayList<>();
        clothes = new ArrayList<>();
        loadItems();
    }

    // Dodavanje itema u prodavnicu
    private void loadItems() {
        // === Napitci ===
        potions.add(new Potion("Potion +20% power (disposable)", true, 0.20, 50, 1));
        potions.add(new Potion("Potion +40% power (disposable)", true, 0.40, 70, 1));
        potions.add(new Potion("Potion +5% power (permanent)", false, 0.05, 200, -1));
        potions.add(new Potion("Potion +10% power (permanent)", false, 0.10, 1000, -1));

        // === Odeća ===
        clothes.add(new Clothing("Gloves (+10% power)", "gloves", 0.10, 60));
        clothes.add(new Clothing("Shield (+10% successful attack)", "shield", 0.10, 60));
        clothes.add(new Clothing("Boots (+40% chance for additional attack)", "boots", 0.40, 80));
    }

    public List<Potion> getPotions() {
        return potions;
    }

    public List<Clothing> getClothes() {
        return clothes;
    }
}
