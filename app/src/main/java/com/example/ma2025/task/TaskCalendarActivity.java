package com.example.ma2025.task;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.ma2025.R;
import com.example.ma2025.auth.AuthManager;
import com.example.ma2025.category.CategoryRepository;
import com.example.ma2025.model.Category;
import com.example.ma2025.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private CategoryRepository categoryRepository;
    private Map<String, Category> categoryMap = new HashMap<>();
    private TaskRepository taskRepository;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);

        calendarView = findViewById(R.id.calendarView);
        categoryRepository = new CategoryRepository();
        taskRepository = new TaskRepository();

        currentUserId = AuthManager.getCurrentUser(this).getUid();

        loadCategoriesThenTasks();

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();
            showTasksForDate(clicked);
        });
    }

    private void loadCategoriesThenTasks() {
        categoryRepository.getCategoriesByUserId(currentUserId)
                .get()
                .addOnSuccessListener(query -> {
                    categoryMap.clear();
                    for (var doc : query) {
                        Category c = doc.toObject(Category.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            categoryMap.put(c.getId(), c);
                        }
                    }
                    // Tek kad se kategorije učitaju — učitaj zadatke
                    loadTasksToCalendar();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Greška pri učitavanju kategorija", Toast.LENGTH_SHORT).show());
    }

    private void loadTasksToCalendar() {
        taskRepository.getAllTasksByUserId(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<EventDay> events = new ArrayList<>();

                    for (var doc : snapshot) {
                        Task task = doc.toObject(Task.class);
                        task.setId(doc.getId());

                        if (task.getExecutionDate() != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(task.getExecutionDate());

                            int color = getCategoryColor(task.getCategoryId());
                            Drawable dotDrawable = createColoredDot(getCategoryColor(task.getCategoryId()));
                            events.add(new EventDay(cal, dotDrawable));
                            //events.add(new EventDay(cal, R.drawable.ic_dot, color));
                        }
                    }

                    calendarView.setEvents(events);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Greška pri učitavanju zadataka", Toast.LENGTH_SHORT).show());
    }

    private int getCategoryColor(String categoryId) {
        Category cat = categoryMap.get(categoryId);
        if (cat != null && cat.getColor() != null) {
            try {
                return Color.parseColor(cat.getColor());
            } catch (Exception e) {
                return Color.GRAY;
            }
        }
        return Color.GRAY;
    }

    private void showTasksForDate(Calendar clicked) {
        FirebaseFirestore.getInstance().collection("tasks")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Task> tasksForDate = new ArrayList<>();

                    for (var doc : snapshot) {
                        Task task = doc.toObject(Task.class);
                        if (task.getExecutionDate() == null) continue;

                        Calendar execCal = Calendar.getInstance();
                        execCal.setTime(task.getExecutionDate());

                        boolean sameDay = execCal.get(Calendar.YEAR) == clicked.get(Calendar.YEAR)
                                && execCal.get(Calendar.DAY_OF_YEAR) == clicked.get(Calendar.DAY_OF_YEAR);

                        if (sameDay) tasksForDate.add(task);
                    }

                    if (tasksForDate.isEmpty()) {
                        Toast.makeText(this, "Nema zadataka za ovaj dan", Toast.LENGTH_SHORT).show();
                    } else {
                        StringBuilder msg = new StringBuilder("Zadaci:\n");
                        for (Task t : tasksForDate) {
                            msg.append("• ").append(t.getName()).append("\n");
                        }
                        Toast.makeText(this, msg.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private Drawable createColoredDot(int color) {
        ShapeDrawable dot = new ShapeDrawable(new OvalShape());
        dot.setIntrinsicHeight(32);
        dot.setIntrinsicWidth(32);
        dot.getPaint().setColor(color);
        return dot;
    }
}
