package com.example.ma2025.model;

public class Potion {
    private String name;
    private boolean isSingleUse;   // true = jednokratni, false = trajni
    private double powerBonus;     // procenat snage (npr. 0.20 = +20%)
    private int price;             // cena u novčićima
    private int duration;          // koliko borbi traje (1 za jednokratni, -1 za trajni)
    private boolean isActivated;   // da li je aktiviran

    public Potion(){}
    public Potion(String name, boolean isSingleUse, double powerBonus, int price, int duration) {
        this.name = name;
        this.isSingleUse = isSingleUse;
        this.powerBonus = powerBonus;
        this.price = price;
        this.duration = duration;
        this.isActivated = false;
    }

    public String getName() {
        return name;
    }

    public boolean isSingleUse() {
        return isSingleUse;
    }

    public double getPowerBonus() {
        return powerBonus;
    }

    public int getPrice() {
        return price;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public void decreaseDuration() {
        if (duration > 0) {
            duration--;
        }
    }

    public boolean isExpired() {
        return duration == 0;
    }

    @Override
    public String toString() {
        return name + " (Bonus: " + (powerBonus * 100) + "%, Cena: " + price + ", Trajanje: " +
                (isSingleUse ? "Jednokratni" : (duration == -1 ? "Trajni" : duration + " borbi")) + ")";
    }
}

