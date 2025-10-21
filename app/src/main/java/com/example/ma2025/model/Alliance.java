package com.example.ma2025.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Alliance {

    private String id;                    // Jedinstveni ID saveza
    private String name;                  // Naziv saveza
    private String leaderId;              // ID vođe (korisnik koji je kreirao savez)
    private String leaderEmail;           // Email vođe
    private List<String> memberIds;       // Lista ID-jeva svih članova
    private List<String> memberEmails;    // Lista email-ova članova
    private boolean missionActive;        // Da li je misija pokrenuta
    private Date createdAt;               // Datum kreiranja
    private String status;                // "active" ili "disbanded"

    // Prazan konstruktor (obavezan za Firebase)
    public Alliance() {
        this.memberIds = new ArrayList<>();
        this.memberEmails = new ArrayList<>();
        this.missionActive = false;
        this.status = "active";
    }

    // Konstruktor sa parametrima
    public Alliance(String id, String name, String leaderId, String leaderEmail) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
        this.leaderEmail = leaderEmail;
        this.memberIds = new ArrayList<>();
        this.memberEmails = new ArrayList<>();
        this.memberIds.add(leaderId);
        this.memberEmails.add(leaderEmail);
        this.missionActive = false;
        this.createdAt = new Date();
        this.status = "active";
    }

    // Getteri i Setteri
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

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderEmail() {
        return leaderEmail;
    }

    public void setLeaderEmail(String leaderEmail) {
        this.leaderEmail = leaderEmail;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getMemberEmails() {
        return memberEmails;
    }

    public void setMemberEmails(List<String> memberEmails) {
        this.memberEmails = memberEmails;
    }

    public boolean isMissionActive() {
        return missionActive;
    }

    public void setMissionActive(boolean missionActive) {
        this.missionActive = missionActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Pomoćne metode
    public void addMember(String memberId, String memberEmail) {
        if (!memberIds.contains(memberId)) {
            memberIds.add(memberId);
            memberEmails.add(memberEmail);
        }
    }

    public void removeMember(String memberId, String memberEmail) {
        int index = memberIds.indexOf(memberId);
        if (index != -1) {
            memberIds.remove(index);
            memberEmails.remove(index);
        }
    }

    public boolean isLeader(String userId) {
        return leaderId != null && leaderId.equals(userId);
    }

    public boolean isMember(String userId) {
        return memberIds != null && memberIds.contains(userId);
    }

    public int getMemberCount() {
        return memberIds != null ? memberIds.size() : 0;
    }
}
