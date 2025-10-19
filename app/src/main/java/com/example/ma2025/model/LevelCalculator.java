package com.example.ma2025.model;

public class LevelCalculator {

    // Prvi nivo: maxXP = 200 (tj. potrebno 200 XP da se dostigne LEVEL_1)
    public static final int FIRST_LEVEL_MAX_XP = 5;

    // Vrati max XP (gornju granicu) za zadati nivo (npr. level = 1 -> 200, level = 2 -> 500, ...)
    public static int getMaxXpForLevel(int level) {
        if (level <= 1) return FIRST_LEVEL_MAX_XP;
        double prev = FIRST_LEVEL_MAX_XP;
        for (int l = 2; l <= level; l++) {
            double next = prev * 2.5; // formula: prev * 2 + prev / 2
            prev = roundUpToNextHundred((int)Math.ceil(next));
        }
        return (int) prev;
    }

    // Min XP za nivo = max XP prethodnog nivoa
    public static int getMinXpForLevel(int level) {
        if (level <= 1) return 0;
        return getMaxXpForLevel(level - 1);
    }

    // Helper: zaokruži na prvu narednu stotinu (čeiling na 100)
    private static int roundUpToNextHundred(int value) {
        if (value % 100 == 0) return value;
        return ((value / 100) + 1) * 100;
    }
}

