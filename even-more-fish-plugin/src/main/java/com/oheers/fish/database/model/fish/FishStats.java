package com.oheers.fish.database.model.fish;


import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Fish;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class FishStats {
    @NonNull
    private final String fishName;
    @NonNull
    private final String fishRarity;

    @NonNull
    private final LocalDateTime firstCatchTime;
    @NonNull
    private final UUID discoverer;
    private String discovererName;
    private float shortestLength;
    @NonNull
    private UUID shortestFisher;
    private String shortestFisherName;
    private float longestLength;
    @NonNull
    private UUID longestFisher;
    private String longestFisherName;
    private int quantity;

    public FishStats(@NonNull String fishName, @NonNull String fishRarity, @NonNull LocalDateTime firstCatchTime, @NonNull UUID discoverer, float shortestLength, @NonNull UUID shortestFisher, float longestLength, @NonNull UUID longestFisher, int quantity) {
        this.fishName = fishName;
        this.fishRarity = fishRarity;
        this.firstCatchTime = firstCatchTime;
        this.discoverer = discoverer;
        this.discovererName = FishUtils.getPlayerName(discoverer);
        this.shortestLength = shortestLength;
        this.shortestFisher = shortestFisher;
        this.shortestFisherName = FishUtils.getPlayerName(shortestFisher);
        this.longestLength = longestLength;
        this.longestFisher = longestFisher;
        this.longestFisherName = FishUtils.getPlayerName(longestFisher);
        this.quantity = quantity;
    }

    public FishStats(Fish fish, @NonNull LocalDateTime firstCatchTime, @NonNull UUID discoverer, float shortestLength, @NonNull UUID shortestFisher, float longestLength, @NonNull UUID longestFisher, int quantity) {
        this(
            fish.getName(),
            fish.getRarity().getId(),
            firstCatchTime,
            discoverer,
            shortestLength,
            shortestFisher,
            longestLength,
            longestFisher,
            quantity
        );
    }

    public static FishStats empty(Fish fish, LocalDateTime firstCatchTime) {
        return new FishStats(fish,firstCatchTime,fish.getFishermanUUID(), fish.getLength(),fish.getFishermanUUID(),fish.getLength(), fish.getFishermanUUID(), 0);
    }

    public @NonNull String getFishName() {
        return fishName;
    }

    public @NonNull String getFishRarity() {
        return fishRarity;
    }

    public @NonNull LocalDateTime getFirstCatchTime() {
        return firstCatchTime;
    }

    public @NonNull Timestamp getFirstCatchTimestamp() {
        return Timestamp.valueOf(firstCatchTime);
    }

    public float getShortestLength() {
        return shortestLength;
    }

    public @NonNull UUID getShortestFisher() {
        return shortestFisher;
    }

    public @Nullable String getShortestFisherName() {
        return shortestFisherName;
    }

    public float getLongestLength() {
        return longestLength;
    }

    public @NonNull UUID getLongestFisher() {
        return longestFisher;
    }

    public @Nullable String getLongestFisherName() {
        return longestFisherName;
    }

    public int getQuantity() {
        return quantity;
    }

    public @NonNull UUID getDiscoverer() {
        return discoverer;
    }

    public @Nullable String getDiscovererName() {
        return discovererName;
    }

    public void setShortestLength(float shortestLength) {
        this.shortestLength = shortestLength;
    }

    public void setShortestFisher(@NonNull UUID shortestFisher) {
        this.shortestFisher = shortestFisher;
        this.shortestFisherName = FishUtils.getPlayerName(shortestFisher);
    }

    public void setLongestLength(float longestLength) {
        this.longestLength = longestLength;
    }

    public void setLongestFisher(@NonNull UUID longestFisher) {
        this.longestFisher = longestFisher;
        this.longestFisherName = FishUtils.getPlayerName(longestFisher);
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
        return "FishStats{" +
                "fishName='" + fishName + '\'' +
                ", fishRarity='" + fishRarity + '\'' +
                ", firstCatchTime=" + firstCatchTime +
                ", discoverer=" + discoverer +
                ", shortestLength=" + shortestLength +
                ", shortestFisher=" + shortestFisher +
                ", longestLength=" + longestLength +
                ", longestFisher=" + longestFisher +
                ", quantity=" + quantity +
                '}';
    }
}
