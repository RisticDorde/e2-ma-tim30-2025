package com.example.ma2025.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ma2025.R;
import com.example.ma2025.model.User;
import com.example.ma2025.model.UserStatistics;
import com.example.ma2025.repository.StatisticsRepository;
import com.example.ma2025.repository.UserRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    // Text Views
    private TextView activeDaysStreakText;
    private TextView totalActiveDaysText;
    private TextView longestStreakText;
    private TextView currentStreakText;
    private TextView averageDifficultyText;
    private TextView totalXp7DaysText;
    private TextView missionsStartedText;
    private TextView missionsCompletedText;

    // Charts
    private PieChart tasksDonutChart;
    private BarChart categoryBarChart;
    private HorizontalBarChart difficultyBarChart;
    private LineChart xpLineChart;

    // Loading
    private ProgressBar loadingProgressBar;
    private View contentView;

    private StatisticsRepository statsRepo;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initViews();
        initRepositories();
        loadCurrentUser();
        loadStatistics();
    }

    private void initViews() {
        // Text Views
        activeDaysStreakText = findViewById(R.id.active_days_streak);
        totalActiveDaysText = findViewById(R.id.total_active_days);
        longestStreakText = findViewById(R.id.longest_streak);
        currentStreakText = findViewById(R.id.current_streak);
        averageDifficultyText = findViewById(R.id.average_difficulty_text);
        totalXp7DaysText = findViewById(R.id.total_xp_7days);
        missionsStartedText = findViewById(R.id.missions_started);
        missionsCompletedText = findViewById(R.id.missions_completed);

        // Charts
        tasksDonutChart = findViewById(R.id.tasks_donut_chart);
        categoryBarChart = findViewById(R.id.category_bar_chart);
        difficultyBarChart = findViewById(R.id.difficulty_bar_chart);
        xpLineChart = findViewById(R.id.xp_line_chart);

        // Loading (dodaj u layout ako želiš)
        // loadingProgressBar = findViewById(R.id.loading_progress);
        // contentView = findViewById(R.id.content_layout);
    }

    private void initRepositories() {
        statsRepo = new StatisticsRepository();
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Niste prijavljeni", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = firebaseUser.getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "Greška pri učitavanju profila", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadStatistics() {
        Log.d("PROVERA", "**********UCITAVANJE STATISTIKE");
        if (currentUserId == null) return;
        Log.d("P", "Ispisi id:" + currentUserId);

        // Prikaži loading (opciono)
        showLoading(true);

        statsRepo.getUserStatistics(currentUserId, new StatisticsRepository.StatisticsCallback() {
            @Override
            public void onSuccess(UserStatistics stats) {
                runOnUiThread(() -> {
                    showLoading(false);
                    displayStatistics(stats);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(StatisticsActivity.this,
                            "Greška pri učitavanju statistike: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        // Implementiraj ako imaš progress bar
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void displayStatistics(UserStatistics stats) {
        Log.d("STATS_DEBUG", "Total Tasks Created: " + stats.getTotalTasksCreated());
        Log.d("STATS_DEBUG", "Total Tasks Completed: " + stats.getTotalTasksCompleted());
        Log.d("STATS_DEBUG", "Active Days: " + stats.getActiveDaysStreak());
        Log.d("STATS_DEBUG", "Categories: " + stats.getCompletedTasksByCategory().size());
        Log.d("STATS_DEBUG", "XP Days: " + stats.getXpLast7Days().size());
        // Postavi text view-e
        displayTextStatistics(stats);

        // Kreiraj grafove
        setupTasksDonutChart(stats);
        setupCategoryBarChart(stats);
        setupDifficultyBarChart(stats);
        setupXpLineChart(stats);
    }

    private void displayTextStatistics(UserStatistics stats) {
        activeDaysStreakText.setText(String.valueOf(stats.getActiveDaysStreak()));
        totalActiveDaysText.setText(String.valueOf(stats.getTotalActiveDays()));
        longestStreakText.setText(String.valueOf(stats.getLongestCompletionStreak()));
        currentStreakText.setText(String.valueOf(stats.getCurrentCompletionStreak()));

        String avgDiff = String.format(Locale.getDefault(), "%.1f", stats.getAverageDifficulty());
        averageDifficultyText.setText("Prosečna težina: " + avgDiff + " (" + stats.getMostCommonDifficulty() + ")");

        totalXp7DaysText.setText("Ukupno XP: " + stats.getTotalXpLast7Days() +
                " (prosek: " + String.format(Locale.getDefault(), "%.0f", stats.getAverageXpPerDay()) + "/dan)");

        missionsStartedText.setText(String.valueOf(stats.getSpecialMissionsStarted()));
        missionsCompletedText.setText(String.valueOf(stats.getSpecialMissionsCompleted()));
    }

    /**
     * 1. Donut graf za status zadataka
     */
    private void setupTasksDonutChart(UserStatistics stats) {
        List<PieEntry> entries = new ArrayList<>();

        int completed = stats.getTotalTasksCompleted();
        int incomplete = stats.getTotalTasksIncomplete();
        int cancelled = stats.getTotalTasksCancelled();

        if (completed > 0) entries.add(new PieEntry(completed, "Završeni"));
        if (incomplete > 0) entries.add(new PieEntry(incomplete, "Nezavršeni"));
        if (cancelled > 0) entries.add(new PieEntry(cancelled, "Otkazani"));

        if (entries.isEmpty()) {
            tasksDonutChart.setNoDataText("Nemate zadataka");
            tasksDonutChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Boje
        ArrayList<Integer> colors = new ArrayList<>();
        if (completed > 0) colors.add(Color.rgb(76, 175, 80));   // Zelena
        if (incomplete > 0) colors.add(Color.rgb(255, 152, 0));  // Narandžasta
        if (cancelled > 0) colors.add(Color.rgb(244, 67, 54));   // Crvena
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        tasksDonutChart.setData(data);
        tasksDonutChart.setDrawHoleEnabled(true);
        tasksDonutChart.setHoleRadius(45f);
        tasksDonutChart.setTransparentCircleRadius(50f);
        tasksDonutChart.setDrawCenterText(true);
        tasksDonutChart.setCenterText("Ukupno\n" + stats.getTotalTasksCreated());
        tasksDonutChart.setCenterTextSize(16f);
        tasksDonutChart.setDescription(null);
        tasksDonutChart.getLegend().setEnabled(true);
        tasksDonutChart.getLegend().setTextSize(12f);
        tasksDonutChart.animateY(1000);
        tasksDonutChart.invalidate();
    }

    /**
     * 2. Bar graf za zadatke po kategoriji
     */
    private void setupCategoryBarChart(UserStatistics stats) {
        Map<String, Integer> categoryData = stats.getCompletedTasksByCategory();

        if (categoryData.isEmpty()) {
            categoryBarChart.setNoDataText("Nemate završenih zadataka");
            categoryBarChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : categoryData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Završeni zadaci");
        dataSet.setColor(Color.rgb(33, 150, 243));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        categoryBarChart.setData(data);
        categoryBarChart.setFitBars(true);
        categoryBarChart.setDescription(null);
        categoryBarChart.animateY(1000);

        XAxis xAxis = categoryBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(10f);

        YAxis leftAxis = categoryBarChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);

        categoryBarChart.getAxisRight().setEnabled(false);
        categoryBarChart.getLegend().setEnabled(false);
        categoryBarChart.invalidate();
    }

    /**
     * 3. Horizontal bar graf za distribuciju težine
     */
    private void setupDifficultyBarChart(UserStatistics stats) {
        Map<String, Integer> difficultyData = stats.getDifficultyDistribution();

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        String[] difficulties = {"Easy", "Medium", "Hard", "Expert"};
        int[] colors = {
                Color.rgb(76, 175, 80),
                Color.rgb(255, 193, 7),
                Color.rgb(255, 87, 34),
                Color.rgb(156, 39, 176)
        };

        for (int i = 0; i < difficulties.length; i++) {
            int count = difficultyData.getOrDefault(difficulties[i], 0);
            entries.add(new BarEntry(i, count));
            labels.add(difficulties[i]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Broj zadataka");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        difficultyBarChart.setData(data);
        difficultyBarChart.setFitBars(true);
        difficultyBarChart.setDescription(null);
        difficultyBarChart.animateX(1000);

        YAxis leftAxis = difficultyBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(-0.5f);
        leftAxis.setAxisMaximum(3.5f);
        leftAxis.setTextSize(12f);

        XAxis xAxis = difficultyBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        difficultyBarChart.getAxisRight().setEnabled(false);
        difficultyBarChart.getLegend().setEnabled(false);
        difficultyBarChart.invalidate();
    }

    /**
     * 4. Line graf za XP poslednjih 7 dana
     */
    private void setupXpLineChart(UserStatistics stats) {
        Map<String, Integer> xpData = stats.getXpLast7Days();

        if (xpData.isEmpty()) {
            xpLineChart.setNoDataText("Nemate XP u poslednjih 7 dana");
            xpLineChart.invalidate();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        List<Entry> entries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();

        // Unazad 6 dana do danas (ukupno 7)
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String date = sdf.format(cal.getTime());

            int xp = xpData.getOrDefault(date, 0);
            entries.add(new Entry(6 - i, xp));

            SimpleDateFormat labelFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            dateLabels.add(labelFormat.format(cal.getTime()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "XP");
        dataSet.setColor(Color.rgb(255, 193, 7));
        dataSet.setCircleColor(Color.rgb(255, 193, 7));
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(3f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(255, 235, 59));
        dataSet.setFillAlpha(50);

        LineData data = new LineData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        xpLineChart.setData(data);
        xpLineChart.setDescription(null);
        xpLineChart.animateX(1000);

        XAxis xAxis = xpLineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);

        YAxis leftAxis = xpLineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);

        xpLineChart.getAxisRight().setEnabled(false);
        xpLineChart.getLegend().setEnabled(true);
        xpLineChart.getLegend().setTextSize(12f);
        xpLineChart.invalidate();
    }
}