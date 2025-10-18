package com.example.ma2025.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ma2025.R;
import com.example.ma2025.category.CategoryRepository;
import com.example.ma2025.model.Category;
import com.example.ma2025.model.Task;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneTimeTaskListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private TaskRepository repository;
    private ListenerRegistration listener;
    private CategoryRepository categoryRepository;
    private Map<String, Category> categoryMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryRepository = new CategoryRepository();
        repository = new TaskRepository();

        // prvo napuni categoryMap
        categoryRepository.getAllCategories()
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

                    // sad možeš inicijalizovati adapter sa mapom
                    adapter = new TaskAdapter(taskList, categoryMap);
                    recyclerView.setAdapter(adapter);

                    // Firestore listener za zadatke
                    listener = repository.getAllTasks()
                            .addSnapshotListener((snapshots, e) -> {
                                if (e != null || snapshots == null) return;

                                taskList.clear();
                                for (var doc : snapshots) {
                                    Task task = doc.toObject(Task.class);
                                    task.setId(doc.getId());
                                    if (task.getFrequency() == TaskFrequency.ONETIME && (task.getParentTaskId() == null || task.getParentTaskId().isEmpty())) {
                                        taskList.add(task);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            });
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.remove();
    }
}

