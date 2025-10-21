package com.example.ma2025.model;

import java.util.Date;

public class Friendship {

    private String id;
    private String fromUserId;
    private String fromUserEmail;
    private String fromUsername;
    private String toUserId;
    private String toUserEmail;
    private String toUsername;
    private String status; // "accepted" (odmah prihvaćeno)
    private Date createdAt;
    private Date acceptedAt;

    // Prazan konstruktor (obavezan za Firebase)
    public Friendship() {
        this.status = "accepted";
        this.createdAt = new Date();
        this.acceptedAt = new Date();
    }

    // Konstruktor sa parametrima
    public Friendship(String id, String fromUserId, String fromUserEmail, String fromUsername,
                      String toUserId, String toUserEmail, String toUsername) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.fromUserEmail = fromUserEmail;
        this.fromUsername = fromUsername;
        this.toUserId = toUserId;
        this.toUserEmail = toUserEmail;
        this.toUsername = toUsername;
        this.status = "accepted";
        this.createdAt = new Date();
        this.acceptedAt = new Date();
    }

    // Getteri i Setteri
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserEmail() {
        return fromUserEmail;
    }

    public void setFromUserEmail(String fromUserEmail) {
        this.fromUserEmail = fromUserEmail;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserEmail() {
        return toUserEmail;
    }

    public void setToUserEmail(String toUserEmail) {
        this.toUserEmail = toUserEmail;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    // Pomoćne metode
    public void accept() {
        this.status = "accepted";
        this.acceptedAt = new Date();
    }

    public boolean isAccepted() {
        return "accepted".equals(status);
    }
}
