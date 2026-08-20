package com.oheers.fish.database.data;


import com.oheers.fish.api.fishing.items.IFish;
import org.jspecify.annotations.NonNull;

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

    public FishLogKey(int userId, IFish fish, LocalDateTime dateTime) {
        this.userId = userId;
        this.fishName = fish.getId();
        this.fishRarity = fish.getRarity().getId();
        this.dateTime = dateTime;
    }

    public static @NonNull FishLogKey of(int userId, final String fishName, final String fishRarity, LocalDateTime dateTime) {
        return new FishLogKey(userId, fishName, fishRarity, dateTime);
    }

    public static @NonNull FishLogKey from(final String pattern) {
        String[] split = pattern.split("\\.");
        return new FishLogKey(Integer.parseInt(split[0]), split[1], split[2], LocalDateTime.parse(split[3]));
    }

    public static @NonNull FishLogKey of(int userId, final @NonNull IFish fish, LocalDateTime dateTime) {
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
