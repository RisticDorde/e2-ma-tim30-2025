package com.example.ma2025.task;

import com.example.ma2025.model.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaskRepository {

    private final CollectionReference taskCollection;

    public TaskRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        taskCollection = db.collection("tasks");
    }

    // Dodavanje zadatka
    public void addTask(Task task) {
        DocumentReference newDoc = taskCollection.document();
        task.setId(newDoc.getId()); // dodeli Firestore ID
        newDoc.set(task);
    }

    // Dobavljanje svih zadataka (slušanje realtime promena)
    public CollectionReference getAllTasks() {
        return taskCollection;
    }

    // Ažuriranje postojećeg zadatka
    public void updateTask(Task task) {
        if (task.getId() != null) {
            taskCollection.document(task.getId()).set(task);
        }
    }

    // Brisanje zadatka
    public void deleteTask(String taskId) {
        taskCollection.document(taskId).delete();
    }
}

