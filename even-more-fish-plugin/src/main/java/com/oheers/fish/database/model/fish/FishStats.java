package com.oheers.fish.database.model.fish;


import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class FishStats {
    @NotNull
    private final String fishName;
    @NotNull
    private final String fishRarity;

    @NotNull
    private final LocalDateTime firstCatchTime;
    @NotNull
    private final UUID discoverer;
    private String discovererName;
    private float shortestLength;
    @NotNull
    private UUID shortestFisher;
    private String shortestFisherName;
    private float longestLength;
    @NotNull
    private UUID longestFisher;
    private String longestFisherName;
    private int quantity;

    public FishStats(@NotNull String fishName, @NotNull String fishRarity, @NotNull LocalDateTime firstCatchTime, @NotNull UUID discoverer, float shortestLength, @NotNull UUID shortestFisher, float longestLength, @NotNull UUID longestFisher, int quantity) {
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

    public FishStats(Fish fish, @NotNull LocalDateTime firstCatchTime, @NotNull UUID discoverer, float shortestLength, @NotNull UUID shortestFisher, float longestLength, @NotNull UUID longestFisher, int quantity) {
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
        return new FishStats(fish,firstCatchTime,fish.getFisherman(), fish.getLength(),fish.getFisherman(),fish.getLength(), fish.getFisherman(), 0);
    }

    public @NotNull String getFishName() {
        return fishName;
    }

    public @NotNull String getFishRarity() {
        return fishRarity;
    }

    public @NotNull LocalDateTime getFirstCatchTime() {
        return firstCatchTime;
    }

    public float getShortestLength() {
        return shortestLength;
    }

    public @NotNull UUID getShortestFisher() {
        return shortestFisher;
    }

    public @Nullable String getShortestFisherName() {
        return shortestFisherName;
    }

    public float getLongestLength() {
        return longestLength;
    }

    public @NotNull UUID getLongestFisher() {
        return longestFisher;
    }

    public @Nullable String getLongestFisherName() {
        return longestFisherName;
    }

    public int getQuantity() {
        return quantity;
    }

    public @NotNull UUID getDiscoverer() {
        return discoverer;
    }

    public @Nullable String getDiscovererName() {
        return discovererName;
    }

    public void setShortestLength(float shortestLength) {
        this.shortestLength = shortestLength;
    }

    public void setShortestFisher(@NotNull UUID shortestFisher) {
        this.shortestFisher = shortestFisher;
        this.shortestFisherName = FishUtils.getPlayerName(shortestFisher);
    }

    public void setLongestLength(float longestLength) {
        this.longestLength = longestLength;
    }

    public void setLongestFisher(@NotNull UUID longestFisher) {
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
