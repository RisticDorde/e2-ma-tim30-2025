package com.example.ma2025.task;

import com.example.ma2025.model.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TaskRepository {

    private final CollectionReference taskCollection;

    public TaskRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        taskCollection = db.collection("tasks");
    }

    // Dodavanje zadatka
    //public void addTask(Task task) {
       // DocumentReference newDoc = taskCollection.document();
        //task.setId(newDoc.getId()); // dodeli Firestore ID
        //newDoc.set(task);
    //}

    // 🔹 Dodavanje zadatka (bilo onetime, bilo repetitive)
    public void addTask(Task task) {
        if (task.getFrequency() == TaskFrequency.REPETITIVE) {
            createRepetitiveTask(task);
        } else {
            createSingleTask(task);
        }
    }

    // 🔹 Jednokratni zadatak
    private void createSingleTask(Task task) {
        DocumentReference newDoc = taskCollection.document();
        task.setId(newDoc.getId());
        newDoc.set(task);
    }

    // 🔹 Kreiranje ponavljajućeg zadatka (template + instance)
    private void createRepetitiveTask(Task templateTask) {
        DocumentReference parentRef = taskCollection.document();
        templateTask.setId(parentRef.getId());
        parentRef.set(templateTask)
                .addOnSuccessListener(v -> {
                    LocalDate start = templateTask.getStartDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate end = templateTask.getEndDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();

                    LocalDate current = start;
                    while (!current.isAfter(end)) {
                        Task instance = new Task();
                        instance.setName(templateTask.getName());
                        instance.setDescription(templateTask.getDescription());
                        instance.setCategoryId(templateTask.getCategoryId());
                        instance.setExecutionDate(Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        instance.setFrequency(TaskFrequency.ONETIME);
                        instance.setParentTaskId(parentRef.getId());
                        instance.setDifficulty(templateTask.getDifficulty());
                        instance.setImportance(templateTask.getImportance());
                        instance.setTotalXP(templateTask.getTotalXP());

                        taskCollection.add(instance);

                        switch (templateTask.getIntervalUnit()) {
                            case DAY:
                                current = current.plusDays(templateTask.getInterval());
                                break;
                            case WEEK:
                                current = current.plusWeeks(templateTask.getInterval());
                                break;
                            case MONTH:
                                current = current.plusMonths(templateTask.getInterval());
                                break;
                        }
                    }
                });
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
    //public void deleteTask(String taskId) {
        //taskCollection.document(taskId).delete();
    //}

    public void deleteTask(Task task) {
        if (task.getId() == null) return;

        // ako je ponavljajući — obriši sve instance koje imaju njega kao parenta
        if (task.getFrequency() == TaskFrequency.REPETITIVE) {
            taskCollection
                    .whereEqualTo("parentTaskId", task.getId())
                    .get()
                    .addOnSuccessListener(query -> {
                        for (var doc : query.getDocuments()) {
                            doc.getReference().delete();
                        }
                    });
        }

        // obriši i sam template
        taskCollection.document(task.getId()).delete();
    }

    public DocumentReference getTaskById(String taskId) {
        return taskCollection.document(taskId);
    }
}

