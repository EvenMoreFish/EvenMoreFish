package com.oheers.fish.database.data;


import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class FishLogKey {
    private int userId;
    private String fishName;
    private String fishRarity;
    private LocalDateTime dateTime;

    public FishLogKey(int userId, String fishName, String fishRarity, LocalDateTime dateTime) {
        this.userId = userId;
        this.fishName = fishName;
        this.fishRarity = fishRarity;
        this.dateTime = dateTime;
    }

    public FishLogKey(int userId, Fish fish, LocalDateTime dateTime) {
        this.userId = userId;
        this.fishName = fish.getName();
        this.fishRarity = fish.getRarity().getId();
        this.dateTime = dateTime;
    }

    public static @NotNull FishLogKey of(int userId, final String fishName, final String fishRarity, LocalDateTime dateTime) {
        return new FishLogKey(userId, fishName, fishRarity, dateTime);
    }

    public static @NotNull FishLogKey from(final String pattern) {
        String[] split = pattern.split("\\.");
        return new FishLogKey(Integer.parseInt(split[0]), split[1], split[2], LocalDateTime.parse(split[3]));
    }

    public static @NotNull FishLogKey of(int userId, final @NotNull Fish fish, LocalDateTime dateTime) {
        return new FishLogKey(userId, fish, dateTime);
    }

    @Override
    public String toString() {
        return userId + "." + fishName + "." + fishRarity + "." + dateTime.toString();
    }
    public String toStringDefault() {
        return "FishLogKey{" +
                "userId=" + userId +
                ", fishName='" + fishName + '\'' +
                ", fishRarity='" + fishRarity + '\'' +
                ", dateTime=" + dateTime +
                '}';
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
