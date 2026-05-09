package com.oheers.fish.api.reward;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Reward {

    // A cache for rewards that could not be given because a player was offline.
    private static final Map<UUID, List<RewardData>> rewardCache = new HashMap<>();

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
        RewardType rewardType = EMFRegistry.REWARD_TYPE.get(this.key);
        if (rewardType != null) {
            this.rewardType = rewardType;
        }
    }

    public RewardType getRewardType() {
        return this.rewardType;
    }

    public @NotNull String getKey() { return this.key; }

    public @NotNull String getValue() { return this.value; }

    public void rewardPlayer(@NotNull Player player, Location hookLocation) {
        getRewardType().doReward(player, getKey(), getValue(), hookLocation);
    }

    public void rewardPlayer(@NotNull OfflinePlayer player, @Nullable Location hookLocation) {
        if (getRewardType() == null) {
            EMFPlugin.getInstance().getLogger().warning("No reward type found for key: " + getKey());
            return;
        }
        rewardPlayer(player, hookLocation, true);
    }

    public void rewardPlayer(@NotNull OfflinePlayer player, @Nullable Location hookLocation, boolean shouldAddToCache) {
        if (getRewardType() == null) {
            EMFPlugin.getInstance().getLogger().warning("No reward type found for key: " + getKey());
            return;
        }
        Player online = player.getPlayer();
        if (online != null) {
            rewardPlayer(online, hookLocation);
            return;
        }
        if (shouldAddToCache) {
            Logging.debug("Player " + player.getUniqueId() + " was not online. Caching their rewards.");
            addToCache(player.getUniqueId(), hookLocation);
        }
    }

    private void addToCache(@NotNull UUID uuid, @Nullable Location hookLocation) {
        List<RewardData> cached = rewardCache.computeIfAbsent(uuid, u -> new ArrayList<>());
        cached.add(new RewardData(this, hookLocation));
    }

    public static void checkCache(@NotNull UUID uuid) {
        if (!rewardCache.containsKey(uuid)) {
            Logging.debug("Player has no cached rewards.");
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            Logging.debug("Player was not online. Cannot give rewards.");
            return;
        }
        Logging.debug("Found cached rewards for " + uuid + ". Giving them all now.");
        List<RewardData> cached = rewardCache.remove(uuid);
        cached.forEach(data -> data.reward().rewardPlayer(player, data.hookLocation()));
    }

    public void setFishVelocity(@Nullable Vector fishVelocity) {
        this.fishVelocity = fishVelocity;
    }

    public @Nullable Vector getFishVelocity() {
        return this.fishVelocity;
    }

    record RewardData(@NotNull Reward reward, @Nullable Location hookLocation) {}

}
