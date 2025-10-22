package com.example.ma2025.model;

import java.util.ArrayList;
import java.util.List;

public class Shop {
    private List<Potion> potions;
    private List<Clothing> clothes;
    private int currentBossIndex;

    public Shop() {}
    public Shop(int currentBossIndex) {
        this.currentBossIndex = currentBossIndex;
        potions = new ArrayList<>();
        clothes = new ArrayList<>();
        loadItems();
    }

    private void loadItems() {
        // Izračunaj nagradu od prethodnog bossa
        long previousBossReward = computeCoinsForBossIndex(currentBossIndex - 1);

        // === Napitci ===
        // 50% od nagrade
        int price1 = (int) (previousBossReward * 0.50);
        potions.add(new Potion("Potion +20% power (disposable)", true, 0.20, price1, 1));

        // 70% od nagrade
        int price2 = (int) (previousBossReward * 0.70);
        potions.add(new Potion("Potion +40% power (disposable)", true, 0.40, price2, 1));

        // 200% od nagrade
        int price3 = (int) (previousBossReward * 2.0);
        potions.add(new Potion("Potion +5% power (permanent)", false, 0.05, price3, -1));

        // 1000% od nagrade
        int price4 = (int) (previousBossReward * 10.0);
        potions.add(new Potion("Potion +10% power (permanent)", false, 0.10, price4, -1));

        // === Odeća ===
        // 60% od nagrade
        int clothingPrice1 = (int) (previousBossReward * 0.60);
        clothes.add(new Clothing("Gloves (+10% power)", "gloves", 0.10, clothingPrice1));
        clothes.add(new Clothing("Shield (+10% successful attack)", "shield", 0.10, clothingPrice1));

        // 80% od nagrade
        int clothingPrice2 = (int) (previousBossReward * 0.80);
        clothes.add(new Clothing("Boots (+40% chance for additional attack)", "boots", 0.40, clothingPrice2));
    }

    private long computeCoinsForBossIndex(int index) {
        if (index <= 0) return 200; // Default za prvi nivo
        double coins = 200;
        for (int i = 1; i < index; i++) {
            coins *= 1.2;
        }
        return Math.round(coins);
    }

    public List<Potion> getPotions() {
        return potions;
    }

    public List<Clothing> getClothes() {
        return clothes;
    }

    // Metoda za dobijanje cene unapređenja oružja
    public int getWeaponUpgradePrice() {
        long previousBossReward = computeCoinsForBossIndex(currentBossIndex - 1);
        return (int) (previousBossReward * 0.60);
    }
}