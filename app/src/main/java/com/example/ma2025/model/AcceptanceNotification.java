package com.example.ma2025.model;

import java.util.Date;

public class AcceptanceNotification {

    private String id;
    private String allianceId;
    private String allianceName;
    private String leaderEmail;
    private String acceptedUserEmail;
    private String acceptedUsername;
    private boolean seen;
    private Date createdAt;

    public AcceptanceNotification() {
        this.seen = false;
        this.createdAt = new Date();
    }

    public AcceptanceNotification(String id, String allianceId, String allianceName,
                                  String leaderEmail, String acceptedUserEmail, String acceptedUsername) {
        this.id = id;
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.leaderEmail = leaderEmail;
        this.acceptedUserEmail = acceptedUserEmail;
        this.acceptedUsername = acceptedUsername;
        this.seen = false;
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

    public String getLeaderEmail() {
        return leaderEmail;
    }

    public void setLeaderEmail(String leaderEmail) {
        this.leaderEmail = leaderEmail;
    }

    public String getAcceptedUserEmail() {
        return acceptedUserEmail;
    }

    public void setAcceptedUserEmail(String acceptedUserEmail) {
        this.acceptedUserEmail = acceptedUserEmail;
    }

    public String getAcceptedUsername() {
        return acceptedUsername;
    }

    public void setAcceptedUsername(String acceptedUsername) {
        this.acceptedUsername = acceptedUsername;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}