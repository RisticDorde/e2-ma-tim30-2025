package com.example.ma2025.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa koja sadrži sve statističke podatke korisnika
 */
public class UserStatistics {

    // Aktivnost korisnika
    private int activeDaysStreak;           // Broj uzastopnih dana korišćenja
    private int totalActiveDays;            // Ukupan broj dana korišćenja

    // Zadaci - ukupno
    private int totalTasksCreated;          // Ukupno kreiranih zadataka
    private int totalTasksCompleted;        // Ukupno završenih zadataka
    private int totalTasksIncomplete;       // Ukupno nezavršenih zadataka
    private int totalTasksCancelled;        // Ukupno otkazanih zadataka

    // Nizovi zadataka
    private int longestCompletionStreak;    // Najduži niz završenih zadataka
    private int currentCompletionStreak;    // Trenutni niz završenih zadataka

    // Zadaci po kategoriji
    private Map<String, Integer> completedTasksByCategory;  // Broj završenih zadataka po kategoriji

    // Prosečna težina
    private double averageDifficulty;       // Prosečna težina završenih zadataka
    private Map<String, Integer> difficultyDistribution; // Distribucija težina (easy, medium, hard, expert)

    // XP u poslednjih 7 dana
    private Map<String, Integer> xpLast7Days;  // Datum -> XP
    private int totalXpLast7Days;

    // Specijalne misije (boss fights)
    private int specialMissionsStarted;     // Broj započetih specijalnih misija
    private int specialMissionsCompleted;   // Broj završenih specijalnih misija
    private int currentBossAttempts;        // Broj pokušaja trenutnog bossa

    public UserStatistics() {
        this.completedTasksByCategory = new HashMap<>();
        this.difficultyDistribution = new HashMap<>();
        this.xpLast7Days = new HashMap<>();

        // Inicijalizacija težina
        difficultyDistribution.put("Easy", 0);
        difficultyDistribution.put("Medium", 0);
        difficultyDistribution.put("Hard", 0);
        difficultyDistribution.put("Expert", 0);
    }

    // Getters i Setters

    public int getActiveDaysStreak() {
        return activeDaysStreak;
    }

    public void setActiveDaysStreak(int activeDaysStreak) {
        this.activeDaysStreak = activeDaysStreak;
    }

    public int getTotalActiveDays() {
        return totalActiveDays;
    }

    public void setTotalActiveDays(int totalActiveDays) {
        this.totalActiveDays = totalActiveDays;
    }

    public int getTotalTasksCreated() {
        return totalTasksCreated;
    }

    public void setTotalTasksCreated(int totalTasksCreated) {
        this.totalTasksCreated = totalTasksCreated;
    }

    public int getTotalTasksCompleted() {
        return totalTasksCompleted;
    }

    public void setTotalTasksCompleted(int totalTasksCompleted) {
        this.totalTasksCompleted = totalTasksCompleted;
    }

    public int getTotalTasksIncomplete() {
        return totalTasksIncomplete;
    }

    public void setTotalTasksIncomplete(int totalTasksIncomplete) {
        this.totalTasksIncomplete = totalTasksIncomplete;
    }

    public int getTotalTasksCancelled() {
        return totalTasksCancelled;
    }

    public void setTotalTasksCancelled(int totalTasksCancelled) {
        this.totalTasksCancelled = totalTasksCancelled;
    }

    public int getLongestCompletionStreak() {
        return longestCompletionStreak;
    }

    public void setLongestCompletionStreak(int longestCompletionStreak) {
        this.longestCompletionStreak = longestCompletionStreak;
    }

    public int getCurrentCompletionStreak() {
        return currentCompletionStreak;
    }

    public void setCurrentCompletionStreak(int currentCompletionStreak) {
        this.currentCompletionStreak = currentCompletionStreak;
    }

    public Map<String, Integer> getCompletedTasksByCategory() {
        return completedTasksByCategory;
    }

    public void setCompletedTasksByCategory(Map<String, Integer> completedTasksByCategory) {
        this.completedTasksByCategory = completedTasksByCategory;
    }

    public double getAverageDifficulty() {
        return averageDifficulty;
    }

    public void setAverageDifficulty(double averageDifficulty) {
        this.averageDifficulty = averageDifficulty;
    }

    public Map<String, Integer> getDifficultyDistribution() {
        return difficultyDistribution;
    }

    public void setDifficultyDistribution(Map<String, Integer> difficultyDistribution) {
        this.difficultyDistribution = difficultyDistribution;
    }

    public Map<String, Integer> getXpLast7Days() {
        return xpLast7Days;
    }

    public void setXpLast7Days(Map<String, Integer> xpLast7Days) {
        this.xpLast7Days = xpLast7Days;
    }

    public int getTotalXpLast7Days() {
        return totalXpLast7Days;
    }

    public void setTotalXpLast7Days(int totalXpLast7Days) {
        this.totalXpLast7Days = totalXpLast7Days;
    }

    public int getSpecialMissionsStarted() {
        return specialMissionsStarted;
    }

    public void setSpecialMissionsStarted(int specialMissionsStarted) {
        this.specialMissionsStarted = specialMissionsStarted;
    }

    public int getSpecialMissionsCompleted() {
        return specialMissionsCompleted;
    }

    public void setSpecialMissionsCompleted(int specialMissionsCompleted) {
        this.specialMissionsCompleted = specialMissionsCompleted;
    }

    public int getCurrentBossAttempts() {
        return currentBossAttempts;
    }

    public void setCurrentBossAttempts(int currentBossAttempts) {
        this.currentBossAttempts = currentBossAttempts;
    }

    // Helper metode

    /**
     * Vraća procenat završenih zadataka
     */
    public float getCompletionRate() {
        if (totalTasksCreated == 0) return 0;
        return (float) totalTasksCompleted / totalTasksCreated * 100;
    }

    /**
     * Vraća dominantnu težinu zadataka
     */
    public String getMostCommonDifficulty() {
        String mostCommon = "Easy";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : difficultyDistribution.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }

        return mostCommon;
    }

    /**
     * Vraća prosečan XP po danu u poslednjih 7 dana
     */
    public double getAverageXpPerDay() {
        if (xpLast7Days.isEmpty()) return 0;
        return (double) totalXpLast7Days / xpLast7Days.size();
    }
}