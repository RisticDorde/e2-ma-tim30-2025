package com.example.ma2025.model;

public class Weapon {
    private String name;
    private String type;          // "sword" ili "bow"
    private double bonus;         // npr. 0.05 = +5%
    private boolean isOwned;      // da li korisnik poseduje
    private double dropChance;    // verovatnoća da padne (ako ga već ima, raste za 0.02%)
    private int upgradeLevel;     // koliko puta je unapređeno

    // Konstruktor
    public Weapon(String name, String type, double bonus, double dropChance) {
        this.name = name;
        this.type = type;
        this.bonus = bonus;
        this.isOwned = false;
        this.dropChance = dropChance; // inicijalna verovatnoća
        this.upgradeLevel = 0;
    }

    // Getteri i setteri
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getBonus() {
        return bonus;
    }

    public boolean isOwned() {
        return isOwned;
    }

    public void setOwned(boolean owned) {
        isOwned = owned;
    }

    public double getDropChance() {
        return dropChance;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    // Kada ponovo dobije isto oružje od bosa
    public void increaseDropChance() {
        dropChance += 0.02;
    }

    // Kada unaprediš oružje
    public void upgrade() {
        upgradeLevel++;
        dropChance += 0.01;
    }

    @Override
    public String toString() {
        return name + " (Bonus: " + (bonus * 100) + "%, Level: " + upgradeLevel +
                ", DropChance: " + dropChance + "%, Owned: " + isOwned + ")";
    }
}

