package com.example.ma2025.task;

public enum TaskImportance {
    NORMAL(1),
    IMPORTANT(3),
    EXTREME_IMPORTANT(10),
    SPECIAL(100);

    private final int xp;
    TaskImportance(int xp) { this.xp = xp; }
    public int getXp() { return xp; }
}
