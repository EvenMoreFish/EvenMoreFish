package com.oheers.fish.api.reward;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Reward {

    // A cache for rewards that could not be given because a player was offline.
    private static final Map<UUID, List<RewardData>> rewardCache = new HashMap<>();

    private final @NonNull String key;
    private final @NonNull String value;
    private RewardType rewardType = null;
    private Vector fishVelocity;

    public Reward(@NonNull String identifier) {
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

    public @NonNull String getKey() { return this.key; }

    public @NonNull String getValue() { return this.value; }

    public void rewardPlayer(@NonNull Player player, Location hookLocation) {
        getRewardType().doReward(player, getKey(), getValue(), hookLocation);
    }

    public void rewardPlayer(@NonNull OfflinePlayer player, @Nullable Location hookLocation) {
        if (getRewardType() == null) {
            EMFPlugin.getInstance().getLogger().warning("No reward type found for key: " + getKey());
            return;
        }
        rewardPlayer(player, hookLocation, true);
    }

    public void rewardPlayer(@NonNull OfflinePlayer player, @Nullable Location hookLocation, boolean shouldAddToCache) {
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

    private void addToCache(@NonNull UUID uuid, @Nullable Location hookLocation) {
        List<RewardData> cached = rewardCache.computeIfAbsent(uuid, u -> new ArrayList<>());
        cached.add(new RewardData(this, hookLocation));
    }

    public static void checkCache(@NonNull UUID uuid) {
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
        EMFPlugin.getInstance().sendMessage("REWARD_CATCHUP", player);
        List<RewardData> cached = rewardCache.remove(uuid);
        cached.forEach(data -> data.reward().rewardPlayer(player, data.hookLocation()));
    }

    public void setFishVelocity(@Nullable Vector fishVelocity) {
        this.fishVelocity = fishVelocity;
    }

    public @Nullable Vector getFishVelocity() {
        return this.fishVelocity;
    }

    record RewardData(@NonNull Reward reward, @Nullable Location hookLocation) {}

}
