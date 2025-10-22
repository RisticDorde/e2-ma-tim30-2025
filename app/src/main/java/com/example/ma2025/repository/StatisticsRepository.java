package com.example.ma2025.repository;

import android.util.Log;

import com.example.ma2025.model.Task;
import com.example.ma2025.model.User;
import com.example.ma2025.model.UserStatistics;
import com.example.ma2025.task.TaskDifficulty;
import com.example.ma2025.task.TaskFrequency;
import com.example.ma2025.task.TaskStatus;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repozitorijum za izračunavanje i dobijanje korisničke statistike iz Firestore-a
 */
public class StatisticsRepository {

    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateOnlyFormat;

    public StatisticsRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    /**
     * Glavna metoda koja vraća kompletan statistički objekat za korisnika
     */
    public void getUserStatistics(String email, StatisticsCallback callback) {
        UserStatistics stats = new UserStatistics();

        // Učitaj sve zadatke korisnika
        db.collection("tasks")
                .whereEqualTo("userId", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task> allTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Task task = document.toObject(Task.class);
                        Log.d("STATS_DEBUG", "Učitan zadatak: " + task);
                        allTasks.add(task);
                    }

                    Log.d("STATS_DEBUG", "Učitano zadataka: " + allTasks.size());
                    Log.d("STATS_DEBUG", "id usera: " + email); // Debug


                    // Učitaj korisnika za boss podatke
                    db.collection("users")
                            .document(email)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                User user = userDoc.toObject(User.class);

                                // Izračunaj sve statistike
                                calculateAllStatistics(stats, allTasks, user);
                                callback.onSuccess(stats);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("StatisticsRepo", "Error loading user: " + e.getMessage());
                                // Nastavi bez user podataka
                                calculateAllStatistics(stats, allTasks, null);
                                callback.onSuccess(stats);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("StatisticsRepo", "Error loading tasks: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    /**
     * Izračunava sve statistike odjednom
     */
    private void calculateAllStatistics(UserStatistics stats, List<Task> allTasks, User user) {
        // 1. Aktivnost korisnika
        calculateActivityStats(stats, allTasks);

        // 2. Ukupni brojevi zadataka
        calculateTaskCounts(stats, allTasks);

        // 3. Nizovi zadataka
        calculateTaskStreaks(stats, allTasks);

        // 4. Zadaci po kategoriji
        calculateTasksByCategory(stats, allTasks);

        // 5. Prosečna težina
        calculateAverageDifficulty(stats, allTasks);

        // 6. XP u poslednjih 7 dana
        calculateXpLast7Days(stats, allTasks);

        // 7. Specijalne misije (boss fights)
        if (user != null) {
            calculateSpecialMissions(stats, user);
        }
    }

    /**
     * 1. Izračunava dane aktivnosti korisnika
     */
    private void calculateActivityStats(UserStatistics stats, List<Task> allTasks) {
        List<String> activeDates = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getTaskStatus() == TaskStatus.ACCOMPLISHED && task.getExecutionDate() != null) {
                String date = dateOnlyFormat.format(task.getExecutionDate());
                if (!activeDates.contains(date)) {
                    activeDates.add(date);
                }
            }
        }

        java.util.Collections.sort(activeDates);
        stats.setTotalActiveDays(activeDates.size());

        // Izračunaj uzastopne dane
        int currentStreak = calculateConsecutiveDays(activeDates);
        stats.setActiveDaysStreak(currentStreak);
    }

    /**
     * Helper metoda za izračunavanje uzastopnih dana
     */
    private int calculateConsecutiveDays(List<String> dates) {
        if (dates.isEmpty()) return 0;

        Calendar cal = Calendar.getInstance();

        try {
            // Počni od današnjeg datuma i idi unazad
            cal.setTime(new Date());
            String today = dateOnlyFormat.format(cal.getTime());

            int streak = 0;

            // Proveri da li je danas bio aktivan
            if (dates.contains(today)) {
                streak = 1;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                // Proveri da li je juče bio aktivan (tolerišemo jedan dan)
                cal.add(Calendar.DAY_OF_YEAR, -1);
                String yesterday = dateOnlyFormat.format(cal.getTime());
                if (dates.contains(yesterday)) {
                    streak = 1;
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                } else {
                    return 0; // Nema aktivnog niza
                }
            }

            // Nastavi da broji unazad
            while (true) {
                String checkDate = dateOnlyFormat.format(cal.getTime());
                if (dates.contains(checkDate)) {
                    streak++;
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                } else {
                    break;
                }
            }

            return streak;

        } catch (Exception e) {
            Log.e("StatisticsRepo", "Error calculating streak: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 2. Izračunava ukupne brojeve zadataka
     */
    private void calculateTaskCounts(UserStatistics stats, List<Task> allTasks) {
        int created = allTasks.size();
        int completed = 0;
        int incomplete = 0;
        int cancelled = 0;

        for (Task task : allTasks) {
            TaskStatus status = task.getTaskStatus();
            if (status == null) {
                incomplete++;
                continue;
            }

            switch (status) {
                case ACCOMPLISHED:
                    completed++;
                    break;
                case PAUSED:
                case UNACCOMPLISHED:
                case ACTIVE:
                    incomplete++;
                    break;
                case CANCELED:
                    cancelled++;
                    break;
            }
        }

        stats.setTotalTasksCreated(created);
        stats.setTotalTasksCompleted(completed);
        stats.setTotalTasksIncomplete(incomplete);
        stats.setTotalTasksCancelled(cancelled);
    }

    /**
     * 3. Izračunava najduži niz uspešno urađenih zadataka
     */
    private void calculateTaskStreaks(UserStatistics stats, List<Task> allTasks) {
        Map<String, Boolean> completionsByDate = new HashMap<>();
        Map<String, Boolean> failuresByDate = new HashMap<>();

        for (Task task : allTasks) {
            if (task.getExecutionDate() == null) continue;

            String date = dateOnlyFormat.format(task.getExecutionDate());

            if (task.getTaskStatus() == TaskStatus.ACCOMPLISHED) {
                completionsByDate.put(date, true);
            } else if (task.getTaskStatus() == TaskStatus.CANCELED) {
                failuresByDate.put(date, true);
            }
        }

        // Pronađi najraniji i najkasniji datum
        List<String> allDates = new ArrayList<>(completionsByDate.keySet());
        allDates.addAll(failuresByDate.keySet());

        if (allDates.isEmpty()) {
            stats.setLongestCompletionStreak(0);
            stats.setCurrentCompletionStreak(0);
            return;
        }

        java.util.Collections.sort(allDates);

        int longestStreak = 0;
        int currentStreak = 0;

        try {
            Date firstDate = dateOnlyFormat.parse(allDates.get(0));
            Date lastDate = dateOnlyFormat.parse(allDates.get(allDates.size() - 1));

            Calendar cal = Calendar.getInstance();
            cal.setTime(firstDate);

            while (!cal.getTime().after(lastDate)) {
                String checkDate = dateOnlyFormat.format(cal.getTime());

                if (failuresByDate.containsKey(checkDate)) {
                    // Niz se prekida kod otkazanih
                    currentStreak = 0;
                } else if (completionsByDate.containsKey(checkDate)) {
                    // Ima završenih zadataka
                    currentStreak++;
                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak;
                    }
                }
                // Niz se NE prekida ako nema zadataka tog dana!

                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (Exception e) {
            Log.e("StatisticsRepo", "Error calculating streaks: " + e.getMessage());
        }

        stats.setLongestCompletionStreak(longestStreak);
        stats.setCurrentCompletionStreak(currentStreak);
    }

    /**
     * 4. Broji završene zadatke po kategoriji
     */
    private void calculateTasksByCategory(UserStatistics stats, List<Task> allTasks) {
        Map<String, Integer> categoryCount = new HashMap<>();

        for (Task task : allTasks) {
            if (task.getTaskStatus() == TaskStatus.ACCOMPLISHED) {
                String categoryId = task.getCategoryId();
                if (categoryId != null && !categoryId.isEmpty()) {
                    categoryCount.put(categoryId, categoryCount.getOrDefault(categoryId, 0) + 1);
                }
            }
        }

        stats.setCompletedTasksByCategory(categoryCount);
    }

    /**
     * 5. Izračunava prosečnu težinu završenih zadataka
     */
    private void calculateAverageDifficulty(UserStatistics stats, List<Task> allTasks) {
        List<Task> completedTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getTaskStatus() == TaskStatus.ACCOMPLISHED) {
                completedTasks.add(task);
            }
        }

        if (completedTasks.isEmpty()) {
            stats.setAverageDifficulty(0);
            return;
        }

        Map<String, Integer> difficultyCount = new HashMap<>();
        difficultyCount.put("Easy", 0);
        difficultyCount.put("Medium", 0);
        difficultyCount.put("Hard", 0);
        difficultyCount.put("Expert", 0);

        int totalDifficulty = 0;

        for (Task task : completedTasks) {
            TaskDifficulty difficulty = task.getDifficulty();
            if (difficulty != null) {
                String diffName = difficulty.name();
                // Kapitalizuj prvo slovo
                diffName = diffName.substring(0, 1).toUpperCase() +
                        diffName.substring(1).toLowerCase();

                difficultyCount.put(diffName, difficultyCount.getOrDefault(diffName, 0) + 1);

                // Dodaj težinu za prosek (EASY=1, MEDIUM=2, HARD=3, EXPERT=4)
                switch (difficulty) {
                    case VERY_EASY:
                        totalDifficulty += 1;
                        break;
                    case EASY:
                        totalDifficulty += 2;
                        break;
                    case HARD:
                        totalDifficulty += 3;
                        break;
                    case EXTREME:
                        totalDifficulty += 4;
                        break;
                }
            }
        }

        double avgDifficulty = (double) totalDifficulty / completedTasks.size();
        stats.setAverageDifficulty(avgDifficulty);
        stats.setDifficultyDistribution(difficultyCount);
    }

    /**
     * 6. Izračunava XP u poslednjih 7 dana
     */
    private void calculateXpLast7Days(UserStatistics stats, List<Task> allTasks) {
        Calendar cal = Calendar.getInstance();
        Map<String, Integer> xpByDate = new HashMap<>();
        int totalXp = 0;

        // Za poslednjih 7 dana
        for (int i = 0; i < 7; i++) {
            String date = dateOnlyFormat.format(cal.getTime());

            int dailyXp = 0;
            for (Task task : allTasks) {
                if (task.getTaskStatus() == TaskStatus.ACCOMPLISHED &&
                        task.getExecutionDate() != null) {
                    String taskDate = dateOnlyFormat.format(task.getExecutionDate());
                    if (taskDate.equals(date)) {
                        dailyXp += task.getTotalXP();
                    }
                }
            }

            xpByDate.put(date, dailyXp);
            totalXp += dailyXp;

            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        stats.setXpLast7Days(xpByDate);
        stats.setTotalXpLast7Days(totalXp);
    }

    /**
     * 7. Izračunava statistiku specijalnih misija (boss fights)
     */
    private void calculateSpecialMissions(UserStatistics stats, User user) {
        // Broj započetih misija = trenutni boss index (ako je > 0)
        int started = user.getCurrentBossIndex();

        // Broj završenih = svi bossovi pre trenutnog
        int completed = 0;
        if (user.getBossRemainingHp() == 0 && user.getCurrentBossIndex() > 0) {
            completed = user.getCurrentBossIndex() - 1;
        } else if (user.getCurrentBossIndex() > 0) {
            completed = user.getCurrentBossIndex() - 1;
        }

        stats.setSpecialMissionsStarted(started);
        stats.setSpecialMissionsCompleted(completed);
    }

    /**
     * Callback interface za asinhrono dobijanje statistike
     */
    public interface StatisticsCallback {
        void onSuccess(UserStatistics statistics);
        void onFailure(Exception e);
    }
}