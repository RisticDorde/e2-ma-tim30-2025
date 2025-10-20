package com.example.ma2025.model;

public class Friend {
    private String id;          // ID prijatelja (User ID)
    private String username; // Username prijatelja
    private String avatar;      // Resource ID avatara
    private boolean isFriend; // Da li je već prijatelj

    public Friend() {}

    public Friend(String id, String username, String avatar) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
        this.isFriend = false; // default
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}

