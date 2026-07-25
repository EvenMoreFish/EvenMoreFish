package com.oheers.fish.database.model.user;

import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.fishing.items.Fish;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class UserReport {

    private int id;
    private final UUID uuid;
    private FishRarityKey firstFish;
    private FishRarityKey recentFish;
    private FishRarityKey largestFish;
    private FishRarityKey shortestFish;
    private int numFishCaught;
    private int competitionsWon;
    private int competitionsJoined;

    private float largestLength;
    private float shortestLength;
    private float totalFishLength;
    
    private int fishSold;
    private double moneyEarned;

    public UserReport(int id, UUID uuid, FishRarityKey firstFish, FishRarityKey recentFish, FishRarityKey largestFish, FishRarityKey shortestFish, int numFishCaught, int competitionsWon, int competitionsJoined, float largestLength, float shortestLength, float totalFishLength, int fishSold, double moneyEarned) {
        this.id = id;
        this.uuid = uuid;
        this.firstFish = firstFish;
        this.recentFish = recentFish;
        this.largestFish = largestFish;
        this.shortestFish = shortestFish;
        this.numFishCaught = numFishCaught;
        this.competitionsWon = competitionsWon;
        this.competitionsJoined = competitionsJoined;
        this.largestLength = largestLength;
        this.shortestLength = shortestLength;
        this.totalFishLength = totalFishLength;
        this.fishSold = fishSold;
        this.moneyEarned = moneyEarned;
    }

    public UserReport(UUID uuid, FishRarityKey firstFish, FishRarityKey recentFish, FishRarityKey largestFish, FishRarityKey shortestFish, int numFishCaught, int competitionsWon, int competitionsJoined, float largestLength, float shortestLength, float totalFishLength, int fishSold, double moneyEarned) {
        this.uuid = uuid;
        this.firstFish = firstFish;
        this.recentFish = recentFish;
        this.largestFish = largestFish;
        this.shortestFish = shortestFish;
        this.numFishCaught = numFishCaught;
        this.competitionsWon = competitionsWon;
        this.competitionsJoined = competitionsJoined;
        this.largestLength = largestLength;
        this.shortestLength = shortestLength;
        this.totalFishLength = totalFishLength;
        this.fishSold = fishSold;
        this.moneyEarned = moneyEarned;
    }

    public FishRarityKey getFirstFish() {
        return firstFish;
    }

    public void setFirstFish(FishRarityKey firstFish) {
        this.firstFish = firstFish;
    }

    public FishRarityKey getRecentFish() {
        return recentFish;
    }

    public void setRecentFish(FishRarityKey recentFish) {
        this.recentFish = recentFish;
    }

    public FishRarityKey getLargestFish() {
        return largestFish;
    }

    public void setLargestFish(FishRarityKey largestFish) {
        this.largestFish = largestFish;
    }

    public int getNumFishCaught() {
        return numFishCaught;
    }

    public void setNumFishCaught(int numFishCaught) {
        this.numFishCaught = numFishCaught;
    }

    public int getCompetitionsWon() {
        return competitionsWon;
    }

    public void setCompetitionsWon(int competitionsWon) {
        this.competitionsWon = competitionsWon;
    }

    public int getCompetitionsJoined() {
        return competitionsJoined;
    }

    public void setCompetitionsJoined(int competitionsJoined) {
        this.competitionsJoined = competitionsJoined;
    }

    public float getTotalFishLength() {
        return totalFishLength;
    }

    public void setTotalFishLength(float totalFishLength) {
        this.totalFishLength = totalFishLength;
    }

    public float getLargestLength() {
        return largestLength;
    }

    public void setLargestLength(float largestLength) {
        this.largestLength = largestLength;
    }

    /**
     * Increases the number of fish caught by a set amount.
     *
     * @param magnitude How many fish to increase the number caught by.
     */
    public void incrementFishCaught(final int magnitude) {
        this.numFishCaught += magnitude;
    }

    /**
     * Increases the total length of fish the player has caught.
     *
     * @param magnitude The size to increase the total size by.
     */
    public void incrementTotalLength(final float magnitude) {
        this.totalFishLength += magnitude;
    }

    /**
     * Adds a specified number to the number of competitions the user has joined.
     *
     * @param magnitude The size to increase the total number joined by.
     */
    public void incrementCompetitionsJoined(final int magnitude) {
        this.competitionsJoined += magnitude;
    }

    /**
     * Adds a specified number to the number of competitions the user has won.
     *
     * @param magnitude The size to increase the total number won by.
     */
    public void incrementCompetitionsWon(final int magnitude) {
        this.competitionsWon += magnitude;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }
    
    public void incrementFishSold(final int fishSold) {
        this.fishSold += fishSold;
    }
    
    public void incrementMoneyEarned(final double moneyEarned) {
        this.moneyEarned += moneyEarned;
    }
    
    public int getFishSold() {
        return fishSold;
    }
    
    public double getMoneyEarned() {
        return moneyEarned;
    }

    public FishRarityKey getShortestFish() {
        return shortestFish;
    }

    public float getShortestLength() {
        return shortestLength;
    }

    public void setShortestFish(FishRarityKey shortestFish) {
        this.shortestFish = shortestFish;
    }

    public void setShortestLength(float shortestLength) {
        this.shortestLength = shortestLength;
    }

    public void setShortestLengthAndFish(@NonNull Fish fish) {
        this.shortestLength = fish.getLength();
        this.shortestFish = FishRarityKey.of(fish);
    }

    public void setLongestLengthAndFish(@NonNull Fish fish) {
        this.largestLength = fish.getLength();
        this.largestFish = FishRarityKey.of(fish);
    }

    @Override
    public String toString() {
        return "UserReport{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", firstFish=" + firstFish +
                ", recentFish=" + recentFish +
                ", largestFish=" + largestFish +
                ", shortestFish=" + shortestFish +
                ", numFishCaught=" + numFishCaught +
                ", competitionsWon=" + competitionsWon +
                ", competitionsJoined=" + competitionsJoined +
                ", largestLength=" + largestLength +
                ", shortestLength=" + shortestLength +
                ", totalFishLength=" + totalFishLength +
                ", fishSold=" + fishSold +
                ", moneyEarned=" + moneyEarned +
                '}';
    }
}
