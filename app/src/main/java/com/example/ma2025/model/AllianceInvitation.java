package com.example.ma2025.model;

import java.util.Date;

public class AllianceInvitation {

    private String id;                      // Jedinstveni ID poziva
    private String allianceId;              // ID saveza na koji se poziva
    private String allianceName;            // Naziv saveza
    private String fromUserId;              // ID korisnika koji šalje poziv (vođa)
    private String fromUserEmail;           // Email korisnika koji šalje poziv
    private String fromUsername;            // Username korisnika koji šalje poziv
    private String toUserId;                // ID korisnika koji prima poziv
    private String toUserEmail;             // Email korisnika koji prima poziv
    private String status;                  // "pending", "accepted", "rejected"
    private Date createdAt;                 // Datum slanja poziva
    private Date respondedAt;               // Datum odgovora na poziv

    // Prazan konstruktor (obavezan za Firebase)
    public AllianceInvitation() {
        this.status = "pending";
    }

    // Konstruktor sa parametrima
    public AllianceInvitation(String id, String allianceId, String allianceName,
                              String fromUserId, String fromUserEmail, String fromUsername,
                              String toUserId, String toUserEmail) {
        this.id = id;
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.fromUserId = fromUserId;
        this.fromUserEmail = fromUserEmail;
        this.fromUsername = fromUsername;
        this.toUserId = toUserId;
        this.toUserEmail = toUserEmail;
        this.status = "pending";
        this.createdAt = new Date();
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

    public String getAllianceName() {
        return allianceName;
    }

    public void setAllianceName(String allianceName) {
        this.allianceName = allianceName;
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

    public Date getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Date respondedAt) {
        this.respondedAt = respondedAt;
    }

    // Pomoćne metode
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isAccepted() {
        return "accepted".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    public void accept() {
        this.status = "accepted";
        this.respondedAt = new Date();
    }

    public void reject() {
        this.status = "rejected";
        this.respondedAt = new Date();
    }
}