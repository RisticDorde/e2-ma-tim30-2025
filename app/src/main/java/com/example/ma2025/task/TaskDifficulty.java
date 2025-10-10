package com.example.ma2025.task;

public enum TaskDifficulty {
    VERY_EASY(1),
    EASY(3),
    HARD(7),
    EXTREME(20);

    private final int xp;
    TaskDifficulty(int xp) { this.xp = xp; }
    public int getXp() { return xp; }
}
