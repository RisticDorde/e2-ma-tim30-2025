package com.example.ma2025.boss;

public class EquipmentItem {
    private String name;
    private String type; // "weapon" ili "clothing"
    private double bonus;
    private boolean isEquipped;

    public EquipmentItem(String name, String type, double bonus, boolean isEquipped) {
        this.name = name;
        this.type = type;
        this.bonus = bonus;
        this.isEquipped = isEquipped;
    }

    // Getters i setters
    public String getName() { return name; }
    public String getType() { return type; }
    public double getBonus() { return bonus; }
    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }
}
