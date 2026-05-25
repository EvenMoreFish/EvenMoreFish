package com.oheers.fish.database.model.user;


import com.oheers.fish.fishing.items.Fish;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserFishStats {
    private final int userId;
    private String fishName;
    private String fishRarity;

    private final LocalDateTime firstCatchTime;
    private float shortestLength;
    private float longestLength;

    private int quantity;

    public UserFishStats(int userId, String fishName, String fishRarity, LocalDateTime firstCatchTime, float shortestLength, float longestLength, int quantity) {
        this.userId = userId;
        this.fishName = fishName;
        this.fishRarity = fishRarity;
        this.firstCatchTime = firstCatchTime;
        this.shortestLength = shortestLength;
        this.longestLength = longestLength;
        this.quantity = quantity;
    }

    public UserFishStats(int userId, Fish fish, LocalDateTime firstCatchTime) {
        this.userId = userId;
        this.fishName = fish.getName();
        this.fishRarity = fish.getRarity().getId();
        this.firstCatchTime = firstCatchTime;
        this.shortestLength = fish.getLength();
        this.longestLength = fish.getLength();
        this.quantity = 0;
    }

    public int getUserId() {
        return userId;
    }

    public String getFishName() {
        return fishName;
    }

    public String getFishRarity() {
        return fishRarity;
    }

    public LocalDateTime getFirstCatchTime() {
        return firstCatchTime;
    }

    public Timestamp getFirstCatchTimestamp() {
        return Timestamp.valueOf(firstCatchTime);
    }

    public float getShortestLength() {
        return shortestLength;
    }

    public float getLongestLength() {
        return longestLength;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setFishName(String fishName) {
        this.fishName = fishName;
    }

    public void setFishRarity(String fishRarity) {
        this.fishRarity = fishRarity;
    }

    public void setShortestLength(float shortestLength) {
        this.shortestLength = shortestLength;
    }

    public void setLongestLength(float longestLength) {
        this.longestLength = longestLength;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void incrementQuantity(int quantity) {
        this.quantity += quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "FishUserStats{" +
                "user=" + userId +
                ", fishName='" + fishName + '\'' +
                ", fishRarity='" + fishRarity + '\'' +
                ", firstCatchTime=" + firstCatchTime +
                ", shortestLength=" + shortestLength +
                ", longestLength=" + longestLength +
                ", quantity=" + quantity +
                '}';
    }
}
