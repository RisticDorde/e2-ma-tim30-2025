package com.example.ma2025.model;

public class TaskXpCalculator {

    // Uzima baznu vrednost (npr. za level 1) i računa vrednost za zadati level
    // formula: svaki naredni level = prev * 1.5 (zaokruženo)
    public static int getXpForLevel(int baseXpLevel1, int level) {
        double xp = baseXpLevel1;
        for (int l = 2; l <= level; l++) {
            xp = xp * 1.5;
            xp = Math.round(xp); // round na najbliži ceo broj
        }
        return (int) Math.round(xp);
    }
}
