package com.example.ma2025.model;

public class Clothing {
    private String name;
    private String type;        // "gloves", "shield", "boots"
    private double bonus;       // procenat bonusa (0.10 = 10%, 0.40 = 40%)
    private int price;          // cena u novčićima
    private int duration;       // koliko borbi traje
    private boolean isActivated;

    public Clothing(String name, String type, double bonus, int price) {
        this.name = name;
        this.type = type;
        this.bonus = bonus;
        this.price = price;
        this.duration = 2;          // po zadatku traje 2 borbe
        this.isActivated = false;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getBonus() {
        return bonus;
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
        return name + " (Bonus: " + (bonus * 100) + "%, Cena: " + price +
                ", Preostalo borbi: " + duration + ")";
    }
}

