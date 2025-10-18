package com.example.ma2025.model;

import com.example.ma2025.task.TaskDifficulty;
import com.example.ma2025.task.TaskFrequency;
import com.example.ma2025.task.TaskImportance;
import com.example.ma2025.task.TaskIntervalUnit;
import com.example.ma2025.task.TaskStatus;

import java.time.LocalDate;
import java.util.Date;

public class Task {
    private String id;
    private String name;
    private String description;
    private String categoryId;

    private TaskFrequency frequency; //jednokratni ili ponavljajuci
    private int interval;
    private TaskIntervalUnit intervalUnit; //day, week, month

    private Date startDate; //kod repetitive
    private Date endDate; // kod repetitive
    private Date executionDate; // kod onetime

    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private int totalXP;

    private String parentTaskId; // null ako je samostalan inace referenca na repetitive task ciji je dio
    private TaskStatus taskStatus;

    public Task (){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public TaskFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(TaskFrequency frequency) {
        this.frequency = frequency;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public TaskIntervalUnit getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(TaskIntervalUnit intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public TaskImportance getImportance() {
        return importance;
    }

    public void setImportance(TaskImportance importance) {
        this.importance = importance;
    }

    public int getTotalXP() {
        return totalXP;
    }

    public void setTotalXP(int totalXP) {
        this.totalXP = totalXP;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }
}
