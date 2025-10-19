package com.example.ma2025.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String email;
    private String username;
    private String password;
    private int avatar;
    private boolean isActive;
    private String createdAt;

    // Profilni podaci
    private Level level = Level.LEVEL_1;
    private Title title = Title.NOVICE;
    private int powerPoints = 10;
    private int experiencePoints = 0;
    private int coins = 0;

    private List<String> currentEquipment = new ArrayList<>();
    private String qrCode = "";
    private List<String> badges = new ArrayList<>();
    private List<String> equipment = new ArrayList<>();
    private List<Potion> potions = new ArrayList<>();
    private List<Weapon> weapons = new ArrayList<>();
    private List<Clothing> clothings = new ArrayList<>();

    public User() {}

    public User(String email, String username, String password, int avatar) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.isActive = false;
        this.qrCode = "USER_" + System.currentTimeMillis();
        this.title = Title.fromLevel(this.level.getLevel());
        this.coins = 100;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (this.id == 0) {
            this.username = username;
        }
    }

    public int getAvatar() { return avatar; }
    public void setAvatar(int avatar) {
        if (this.id == 0) {
            this.avatar = avatar;
        }
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) {
        this.level = level;
        this.title = Title.fromLevel(level.getLevel());
    }

    public Title getTitle() { return title; }
    public void setTitle(Title title) { this.title = title; }

    public int getLevelNumber() {
        return level.getLevel();
    }

    public String getTitleDisplayName() {
        return title.getDisplayName();
    }

    public int getPowerPoints() { return powerPoints; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }

    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
        Level newLevel = Level.fromExperience(experiencePoints);
        if (newLevel.getLevel() != this.level.getLevel()) {
            setLevel(newLevel);
        }
    }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }

    public List<String> getEquipment() { return equipment; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment; }

    public List<String> getCurrentEquipment() { return currentEquipment; }
    public void setCurrentEquipment(List<String> currentEquipment) { this.currentEquipment = currentEquipment; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public int getCurrentLevelProgress() {
        return experiencePoints - level.getMinXp();
    }

    public int getNextLevelRequiredXp() {
        return level.getMaxXp() - level.getMinXp();
    }

    public float getLevelProgressPercentage() {
        int progress = getCurrentLevelProgress();
        int required = getNextLevelRequiredXp();
        return required > 0 ? (float) progress / required * 100 : 0;
    }

    public void addExperience(int xp) {
        this.experiencePoints += xp;
        Level newLevel = Level.fromExperience(experiencePoints);
        if (newLevel.getLevel() != this.level.getLevel()) {
            setLevel(newLevel);
        }
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }

    public List<Potion> getPotions() {
        return potions;
    }

    public void setPotions(List<Potion> potions) {
        this.potions = potions;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    public List<Clothing> getClothings() {
        return clothings;
    }

    public void setClothings(List<Clothing> clothings) {
        this.clothings = clothings;
    }



    // ENUM za Title
    public enum Title {
        NOVICE("Početnik"),
        ADVENTURER("Avanturista"),
        WARRIOR("Ratnik"),
        MASTER("Majstor"),
        LEGEND("Legenda");

        private final String displayName;

        Title(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        // Dobijanje titule na osnovu levela
        public static Title fromLevel(int level) {
            if (level >= 5) return LEGEND;
            if (level >= 4) return MASTER;
            if (level >= 3) return WARRIOR;
            if (level >= 2) return ADVENTURER;
            return NOVICE;
        }
    }

    // ENUM za Level unutar User klase
    public enum Level {
        LEVEL_1(1, 0, 100),
        LEVEL_2(2, 100, 200),
        LEVEL_3(3, 200, 450),
        LEVEL_4(4, 450, 750),
        LEVEL_5(5, 750, 1000);

        private final int level;
        private final int minXp;
        private final int maxXp;

        Level(int level, int minXp, int maxXp) {
            this.level = level;
            this.minXp = minXp;
            this.maxXp = maxXp;
        }

        public int getLevel() { return level; }
        public int getMinXp() { return minXp; }
        public int getMaxXp() { return maxXp; }

        // Dobijanje levela na osnovu XP
        public static Level fromExperience(int experience) {
            for (Level level : values()) {
                if (experience >= level.minXp && experience < level.maxXp) {
                    return level;
                }
            }
            // Ako je experience veći od najvećeg, vrati najviši level
            return LEVEL_5;
        }

        // Dobijanje Level enum na osnovu broja
        public static Level fromLevelNumber(int levelNumber) {
            for (Level level : values()) {
                if (level.getLevel() == levelNumber) {
                    return level;
                }
            }
            return LEVEL_1; // default
        }
    }
}
