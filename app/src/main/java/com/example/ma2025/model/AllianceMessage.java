package com.example.ma2025.model;

import java.util.Date;

public class AllianceMessage {

    private String id;                    // Jedinstveni ID poruke
    private String allianceId;            // ID saveza kojem pripada
    private String senderId;              // ID pošiljaoca
    private String senderEmail;           // Email pošiljaoca
    private String senderUsername;        // Username pošiljaoca
    private String senderAvatar;          // Avatar pošiljaoca
    private String messageText;           // Tekst poruke
    private Date timestamp;               // Vreme slanja
    private boolean isRead;               // Da li je pročitana (opciono)

    // Prazan konstruktor (obavezan za Firebase)
    public AllianceMessage() {
        this.timestamp = new Date();
        this.isRead = false;
    }

    // Konstruktor sa parametrima
    public AllianceMessage(String allianceId, String senderId, String senderEmail,
                           String senderUsername, String senderAvatar, String messageText) {
        this.allianceId = allianceId;
        this.senderId = senderId;
        this.senderEmail = senderEmail;
        this.senderUsername = senderUsername;
        this.senderAvatar = senderAvatar;
        this.messageText = messageText;
        this.timestamp = new Date();
        this.isRead = false;
    }

    // Getteri i Setteri
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Pomoćna metoda za formatiranje vremena
    public String getFormattedTime() {
        if (timestamp == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(timestamp);
    }

    // Pomoćna metoda za prikaz datuma (ako je poruka starija od danas)
    public String getFormattedDate() {
        if (timestamp == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(timestamp);
    }
}