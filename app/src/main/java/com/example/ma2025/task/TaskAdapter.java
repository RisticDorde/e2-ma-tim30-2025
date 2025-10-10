package com.example.ma2025.task;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.model.Category;
import com.example.ma2025.model.Task;

import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Map<String, Category> categoryMap; // categoryId -> Category

    public TaskAdapter(List<Task> taskList, Map<String, Category> categoryMap) {
        this.taskList = taskList;
        this.categoryMap = categoryMap;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskName.setText(task.getName());
        holder.tvTaskDate.setText(
                task.getFrequency() == null || task.getFrequency() == TaskFrequency.ONETIME
                        ? task.getExecutionDate() != null ? task.getExecutionDate().toString() : "-"
                        : task.getStartDate() + " → " + task.getEndDate()
        );

        // Pronađi kategoriju
        Category cat = categoryMap.get(task.getCategoryId());
        if (cat != null) {
            holder.tvCategoryName.setText(cat.getName());
            try {
                holder.viewCategoryColor.setBackgroundColor(Color.parseColor(cat.getColor()));
            } catch (Exception e) {
                holder.viewCategoryColor.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.tvCategoryName.setText("Nepoznato");
            holder.viewCategoryColor.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvTaskDate, tvCategoryName;
        View viewCategoryColor;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
        }
    }
}
