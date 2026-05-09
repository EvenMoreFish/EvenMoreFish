package com.oheers.fish.database.model.fish;


import com.oheers.fish.fishing.items.Fish;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class FishLog {
    private final int userId;
    private final String fishName;
    private final String fishRarity;
    private final LocalDateTime catchTime;
    private final float length;

    private final @Nullable String competitionId;


    public FishLog(int userId, String fishName, String fishRarity, LocalDateTime catchTime, float length, @Nullable String competitionId) {
        this.userId = userId;
        this.fishName = fishName;
        this.fishRarity = fishRarity;
        this.catchTime = catchTime;
        this.length = length;
        this.competitionId = competitionId;
    }

    public FishLog(int userId, Fish fish, LocalDateTime catchTime, @Nullable String competitionId) {
        this.userId = userId;
        this.fishName = fish.getName();
        this.fishRarity = fish.getRarity().getId();
        this.catchTime = catchTime;
        this.length = fish.getLength();
        this.competitionId = competitionId;
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

    public LocalDateTime getCatchTime() {
        return catchTime;
    }

    public Timestamp getCatchTimestamp() {
        return Timestamp.valueOf(catchTime);
    }

    public float getLength() {
        return length;
    }

    @Nullable
    public String getCompetitionId() {
        return competitionId;
    }

    @Override
    public String toString() {
        return "FishLog{" +
                "userId=" + userId +
                ", fishName='" + fishName + '\'' +
                ", rarity='" + fishRarity + '\'' +
                ", catchTime=" + catchTime +
                ", length=" + length +
                ", competitionId='" + competitionId + '\'' +
                '}';
    }
}
