package com.example.ma2025.boss;


public class Boss {
    private int index;
    private long maxHp;
    private long currentHp;

    public Boss(int index) {
        this.index = index;
        this.maxHp = computeBossHpForIndex(index);
        this.currentHp = this.maxHp;
    }

    public int getIndex() {
        return index;
    }

    public long getMaxHp() {
        return maxHp;
    }

    public long getCurrentHp() {
        return currentHp;
    }

    public boolean isDefeated() {
        return currentHp <= 0;
    }

    public void takeDamage(long amount) {
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
    }

    /**
     * Formula za izračunavanje HP-a bossa po nivou.
     * Možeš menjati eksponent i bazu ako želiš drugačiji scaling.
     */
    public static long computeBossHpForIndex(int index) {
        double baseHp = 1000;     // HP prvog bossa
        double growth = 1.25;     // svaka sledeća borba +25%
        double hp = baseHp * Math.pow(growth, index - 1);
        return Math.round(hp);
    }

    /**
     * Resetuje HP ako igrač ponovo kreće istog bossa.
     */
    public void resetHp() {
        this.currentHp = this.maxHp;
    }
}

