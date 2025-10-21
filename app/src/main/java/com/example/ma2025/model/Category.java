package com.example.ma2025.model;

public class Category {
    private String id;
    private String userId;
    private String name;
    private String color;

    public Category() {}

    public Category(String id, String name, String color, String userId) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.userId = userId;
    }

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
