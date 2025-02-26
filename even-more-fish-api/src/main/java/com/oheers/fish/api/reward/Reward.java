package com.oheers.fish.api.reward;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Reward {

    private final @NotNull String key;
    private final @NotNull String value;
    private RewardType rewardType = null;
    private Vector fishVelocity;

    public Reward(@NotNull String identifier) {
        String[] split = identifier.split(":");
        if (split.length < 2) {
            EMFPlugin.getInstance().getLogger().warning(identifier + " is not formatted correctly. It won't be given as a reward");
            this.key = "";
            this.value = "";
        } else {
            this.key = split[0];
            this.value = String.join(":", Arrays.copyOfRange(split, 1, split.length));
        }
        for (RewardType rewardType : RewardManager.getInstance().getRegisteredRewardTypes()) {
            if (rewardType.isApplicable(this.key)) {
                this.rewardType = rewardType;
                return;
            }
        }
    }

    public RewardType getRewardType() {
        return this.rewardType;
    }

    public @NotNull String getKey() { return this.key; }

    public @NotNull String getValue() { return this.value; }
    
    public void rewardPlayer(@NotNull Player player, Location hookLocation) {
        if (getRewardType() == null) {
            EMFPlugin.getInstance().getLogger().warning("No reward type found for key: " + getKey());
            return;
        }
        getRewardType().doReward(player, getKey(), getValue(), hookLocation);
    }

    public void setFishVelocity(@Nullable Vector fishVelocity) {
        this.fishVelocity = fishVelocity;
    }

    public @Nullable Vector getFishVelocity() {
        return this.fishVelocity;
    }

}
