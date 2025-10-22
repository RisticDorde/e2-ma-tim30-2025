package com.example.ma2025.model;

import android.util.Log;

import com.example.ma2025.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String email;
    private String username;
    private String password;
    private String avatar;
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

    private int currentBossIndex;
    private long bossRemainingHp;
    private String lastLevelUpAt;

    public User()
    {
        this.potions = new ArrayList<>();
        this.clothings = new ArrayList<>();
        this.weapons = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.currentEquipment = new ArrayList<>();
    }

    public User(String email, String username, String password, String avatar) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.isActive = false;
        this.qrCode = "USER_" + System.currentTimeMillis();
        this.title = Title.fromLevel(this.level.getLevelNumber());
        this.coins = 100;
        this.currentBossIndex = 0;
        this.bossRemainingHp = 0;
        this.lastLevelUpAt = String.valueOf(System.currentTimeMillis());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
        this.title = Title.fromLevel(level.getLevelNumber());
    }

    public Title getTitle() { return title; }
    public void setTitle(Title title) { this.title = title; }

    public int getLevelNumber() {
        return level.getLevelNumber();
    }

    public String getTitleDisplayName() {
        return title.getDisplayName();
    }

    public int getPowerPoints() { return powerPoints; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }

    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }

    public List<String> getEquipment() {
        if (equipment == null) {
            equipment = new ArrayList<>();
        }
        return equipment;
    }
    public void setEquipment(List<String> equipment) {
        this.equipment = equipment != null ? equipment : new ArrayList<>();
    }

    public List<String> getCurrentEquipment() {
        if (currentEquipment == null) {
            currentEquipment = new ArrayList<>();
        }
        return currentEquipment;
    }
    public void setCurrentEquipment(List<String> currentEquipment) {
        this.currentEquipment = currentEquipment != null ? currentEquipment : new ArrayList<>();
    }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public int getCurrentLevelProgress() {
        return experiencePoints - LevelCalculator.getMinXpForLevel(this.level.getLevelNumber());
    }

    public int getNextLevelRequiredXp() {
        return LevelCalculator.getMaxXpForLevel(this.level.getLevelNumber()) - LevelCalculator.getMinXpForLevel(this.level.getLevelNumber());
    }

    public float getLevelProgressPercentage() {
        int progress = getCurrentLevelProgress();
        int required = getNextLevelRequiredXp();
        return required > 0 ? (float) progress / required * 100 : 0;
    }
//    public void addExperience(int xpToAdd, boolean defeatedBoss, int coinsRewardIfBoss, UserRepository userRepo) {
//        this.experiencePoints += xpToAdd;
//
//        Log.d("TaskDetails", "XP = " + this.experiencePoints);
//        boolean leveledUp = false;
//        while (true) {
//            int currentLevelNumber = this.level.getLevelNumber();
//            int maxXpForCurrent = LevelCalculator.getMaxXpForLevel(currentLevelNumber);
//
//            if (this.experiencePoints >= maxXpForCurrent) {
//                leveledUp = true;
//                int nextLevelNumber = currentLevelNumber + 1;
//                this.setLevel(Level.fromLevelNumber(nextLevelNumber));
//
//                if (currentLevelNumber == 1) {
//                    // prvi prelazak → 40 PP
//                    this.powerPoints += 40;
//                } else {
//                    // svako sledeće → formula
//                    int previousReward = this.powerPoints; // trenutna vrednost PP
//                    int reward = (int) (previousReward + (0.75 * previousReward));
//                    this.powerPoints = reward;
//                }
//
//                if (defeatedBoss) {
//                    this.coins += coinsRewardIfBoss;
//                }
//
//                // Logika boss index
//                if (this.currentBossIndex == 0 && nextLevelNumber >= 2) {
//                    // otključava se prvi boss
//                    this.currentBossIndex = 1;
//                    this.bossRemainingHp = 200; // prvi boss HP
//                    this.setCurrentBossIndex(1);
//                    this.setBossRemainingHp(200);
//                } else if (nextLevelNumber > 2 && this.bossRemainingHp == 0) {
//                    // prelazak na naredni boss samo ako prethodni boss poražen
//                    this.currentBossIndex = nextLevelNumber - 1;
//                    this.bossRemainingHp = computeBossHpForIndex(this.currentBossIndex);
//                    this.setCurrentBossIndex(nextLevelNumber - 1);
//                    this.setBossRemainingHp(computeBossHpForIndex(this.currentBossIndex));
//                }
//
//            } else {
//                break;
//            }
//        }
//        Log.d("ADD_XP_FINAL", "Saving -> bossIndex=" + this.currentBossIndex +
//                ", bossHP=" + this.bossRemainingHp + ", level=" + this.level.getLevelNumber());
//        userRepo.updateUser(this);
//    }
public void addExperience(int baseXpLevel1, boolean defeatedBoss, int coinsRewardIfBoss, UserRepository userRepo) {
    int xpToAdd = TaskXpCalculator.getXpForLevel(baseXpLevel1, this.level.getLevelNumber());
    this.experiencePoints += xpToAdd;

    Log.d("ADD_XP_START", "XP before: " + this.experiencePoints +
            ", Level=" + this.level.getLevelNumber() +
            ", BossIndex=" + this.currentBossIndex +
            ", BossHP=" + this.bossRemainingHp);

    boolean leveledUp = false;

    while (true) {
        int currentLevelNumber = this.level.getLevelNumber();
        int maxXpForCurrent = LevelCalculator.getMaxXpForLevel(currentLevelNumber);

        if (this.experiencePoints >= maxXpForCurrent) {
            leveledUp = true;
            int nextLevelNumber = currentLevelNumber + 1;
            this.setLevel(Level.fromLevelNumber(nextLevelNumber));

            Log.d("LEVEL_UP", "Leveled up from " + currentLevelNumber + " → " + nextLevelNumber);

            // Power Points nagrada
            if (currentLevelNumber == 1) {
                this.powerPoints += 40;
                Log.d("LEVEL_UP", "Initial PP bonus +40 (now " + this.powerPoints + ")");
            } else {
                int previousReward = this.powerPoints;
                int reward = (int) (previousReward + (0.75 * previousReward));
                this.powerPoints = reward;
                Log.d("LEVEL_UP", "PP scaled from " + previousReward + " → " + reward);
            }

            // Boss nagrada ako je poražen
            if (defeatedBoss) {
                this.coins += coinsRewardIfBoss;
                Log.d("LEVEL_UP", "Boss defeated → +coins=" + coinsRewardIfBoss + ", total=" + this.coins);
            }

        } else {
            break;
        }
    }

    // === Boss logika POSLE level up petlje ===
    int currentLevelNumber = this.level.getLevelNumber();

    if (this.currentBossIndex == 0 && currentLevelNumber >= 2) {
        // otključava se prvi boss
        this.currentBossIndex = 1;
        this.bossRemainingHp = 200;
        this.setCurrentBossIndex(1);
        this.setBossRemainingHp(200);
        Log.d("BOSS_LOGIC", "Unlocked first boss → index=1, HP=200");
    }
    else if (currentLevelNumber > 2 && this.bossRemainingHp == 0 && defeatedBoss) {
        // prelazak na narednog bossa (SAMO ako je boss poražen)
        int newBossIndex = currentLevelNumber - 1;
        int newHp = computeBossHpForIndex(newBossIndex);
        this.currentBossIndex = newBossIndex;
        this.bossRemainingHp = newHp;
        this.setCurrentBossIndex(newBossIndex);
        this.setBossRemainingHp(newHp);
        Log.d("BOSS_LOGIC", "Advanced to boss index=" + newBossIndex + ", HP=" + newHp);
    }

    // Završni log pre čuvanja
    Log.d("ADD_XP_FINAL", "Saving → XP=" + this.experiencePoints +
            ", Level=" + this.level.getLevelNumber() +
            ", BossIndex=" + this.currentBossIndex +
            ", BossHP=" + this.bossRemainingHp +
            ", Coins=" + this.coins +
            ", PP=" + this.powerPoints);

    // Obavezno ažuriranje u repozitorijumu
    userRepo.updateUser(this);
    Log.d("UPDATE", "***************************** Updating user with ID=" + this.id);
}


    // helper za HP bossa po indexu
    private int computeBossHpForIndex(int index) {
        if (index == 1) return 200; // prvi boss
        int prevHp = 200;
        for (int i = 2; i <= index; i++) {
            prevHp = prevHp * 2 + prevHp / 2;
        }
        return prevHp;
    }

    public void addExperience(int xpToAdd, UserRepository userRepo) {
        addExperience(xpToAdd, false, 0, userRepo);
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

    public boolean canFightBoss() {
        Log.d("BossCheck", "currentBossIndex = " + this.currentBossIndex);
        Log.d("BossCheck", "bossRemainingHp = " + this.bossRemainingHp);
        Log.d("BossCheck", "level = " + this.level.getLevelNumber());

        if (this.currentBossIndex == 0) {
            Log.d("BossCheck", "Nema bossa još → vraćam false");
            return false; // još nema bossa
        }
        if (this.bossRemainingHp > 0) {
            Log.d("BossCheck", "Preostao HP bossa → vraćam true");
            return true;    // preostao HP → mora se boriti
        }
        int requiredLevel = this.currentBossIndex + 1; // boss i level logika
        boolean canFight = this.level.getLevelNumber() >= requiredLevel;
        Log.d("BossCheck", "requiredLevel = " + requiredLevel + ", canFight = " + canFight);
        return canFight;
    }

    // Provera da li već ima taj item
    public boolean hasWeapon(String weaponName) {
        for (Weapon w : weapons) {
            if (w.getName().equals(weaponName) && w.isOwned()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasClothing(String clothingName) {
        for (Clothing c : clothings) {
            if (c.getName().equals(clothingName) && c.isOwned())
            {
                return true;
            }
        }
        return false;
    }

    // Dodavanje novog itema
    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public void addClothing(Clothing clothing) {
        clothings.add(clothing);
    }

    // Za aktivnu opremu (equipovano)
    public void equipItem(String itemName) {
        if (!currentEquipment.contains(itemName)) {
            currentEquipment.add(itemName);
        }
    }

    public void unequipItem(String itemName) {
        currentEquipment.remove(itemName);
    }

    public List<Potion> getPotions() {
        if (potions == null) {
            potions = new ArrayList<>();
        }
        return potions;
    }

    public void setPotions(List<Potion> potions) {
        this.potions = potions != null ? potions : new ArrayList<>();
    }
    public List<Weapon> getWeapons() {
        if (weapons == null) {
            weapons = new ArrayList<>();
        }
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons != null ? weapons : new ArrayList<>();
    }
    public List<Clothing> getClothings() {
        if (clothings == null) {
            clothings = new ArrayList<>();
        }
        return clothings;
    }

    public void setClothings(List<Clothing> clothings) {
        this.clothings = clothings != null ? clothings : new ArrayList<>();
    }
    public int getCurrentBossIndex() {
        return currentBossIndex;
    }

    public void setCurrentBossIndex(int currentBossIndex) {
        this.currentBossIndex = currentBossIndex;
    }

    public long getBossRemainingHp() {
        return bossRemainingHp;
    }

    public void setBossRemainingHp(long bossRemainingHp) {
        this.bossRemainingHp = bossRemainingHp;
    }

    public String getLastLevelUpAt() {
        return lastLevelUpAt;
    }

    public void setLastLevelUpAt(String lastLevelUpAt) {
        this.lastLevelUpAt = lastLevelUpAt;
    }

    // ENUM za Title
    public enum Title {
        NOVICE("Beginner"),
        ADVENTURER("Apprentice"),
        WARRIOR("Warrior"),
        MASTER("Knight"),
        LEGEND("Legend");

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

    public enum Level {
        LEVEL_1(1),
        LEVEL_2(2),
        LEVEL_3(3),
        LEVEL_4(4),
        LEVEL_5(5);

        private final int levelNumber;
        Level(int levelNumber)
        {
            this.levelNumber = levelNumber;
        }
        public int getLevelNumber()
        {
            return levelNumber;
        }

        public static Level fromLevelNumber(int levelNumber) {
            for (Level level : values()) {
                if (level.getLevelNumber() == levelNumber) {
                    return level;
                }
            }
            return LEVEL_1; // default ako nije pronađen
        }
    }


}
