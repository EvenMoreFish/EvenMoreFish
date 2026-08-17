package com.oheers.fish.api.requirement;

import com.oheers.fish.api.fishing.FishingType;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class RequirementContext {

    private NamespacedKey worldKey;
    private Location location;
    private UUID player;
    private YamlDocument config;
    private String configPath;
    private FishingType fishingType;

    /**
     * Provides relevant data to be checked in requirements.
     */
    public RequirementContext(@Nullable World world, @Nullable Location location, @Nullable Player player, @Nullable YamlDocument config, @Nullable String configPath, @Nullable FishingType fishingType) {
        this.worldKey = world == null ? null : world.getKey();
        this.location = location;
        this.player = player == null ? null : player.getUniqueId();
        this.config = config;
        this.configPath = configPath;
        this.fishingType = fishingType == null ? FishingType.VANILLA : fishingType;
    }

    public static @NonNull RequirementContext empty() {
        return new RequirementContext(null, null, null, null, null, null);
    }

    public static @NonNull RequirementContext player(@NonNull Player player) {
        return new RequirementContext(
            player.getWorld(),
            player.getLocation(),
            player,
            null,
            null,
            null
        );
    }

    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldKey);
    }

    public void setWorld(@Nullable World world) {
        this.worldKey = world == null ? null : world.getKey();
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public void setConfig(@Nullable YamlDocument config) {
        this.config = config;
    }

    public @Nullable YamlDocument getConfig() {
        return this.config;
    }

    public void setConfigPath(@Nullable String configPath) {
        this.configPath = configPath;
    }

    public @Nullable String getConfigPath() {
        return this.configPath;
    }

    /**
     * Sets the location variable as well as the world variable, the world variable is fetched from the #getWorld() from
     * Location.
     *
     * @param location The location.
     */
    public void setLocation(@Nullable Location location) {
        this.location = location;
        this.worldKey = location == null ? null : location.getWorld().getKey();
    }

    public @Nullable Player getPlayer() {
        return player == null ? null : Bukkit.getPlayer(player);
    }

    public void setPlayer(@Nullable Player player) {
        this.player = player == null ? null : player.getUniqueId();
    }

    /**
     * Attempts to get a location from this context.
     * <p>
     * If {@link #getLocation()} is null, it will try to get the linked player's location.
     */
    public @Nullable Location getHookOrPlayerLocation() {
        if (location != null) {
            return location;
        }
        Player player = getPlayer();
        if (player != null) {
            return player.getLocation();
        }
        return null;
    }

    public @Nullable FishingType getFishingType() {
        return this.fishingType;
    }

    public void setFishingType(@Nullable FishingType fishingType) {
        this.fishingType = fishingType;
    }

}
