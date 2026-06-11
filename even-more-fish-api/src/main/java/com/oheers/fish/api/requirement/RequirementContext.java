package com.oheers.fish.api.requirement;

import com.oheers.fish.api.fishing.FishingType;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class RequirementContext {

    private WeakReference<World> worldRef;
    private Location location;
    private Player player;
    private YamlDocument config;
    private String configPath;
    private FishingType fishingType;

    /**
     * Provides data about the current server to the requirement checker, for example it passes through the current
     * tick time of the day so the in-game time limit can be compared, or the location the player is in so the world
     * can be checked or the regions or y-level can be checked.
     */
    public RequirementContext(@Nullable World world, @Nullable Location location, @Nullable Player player, @Nullable YamlDocument config, @Nullable String configPath, @Nullable FishingType fishingType) {
        this.worldRef = new WeakReference<>(world);
        this.location = location;
        this.player = player;
        this.config = config;
        this.configPath = configPath;
        this.fishingType = fishingType == null ? FishingType.VANILLA : fishingType;
    }

    public @Nullable World getWorld() {
        return worldRef.get();
    }

    public void setWorld(World world) {
        this.worldRef = new WeakReference<>(world);
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public void setConfig(YamlDocument config) {
        this.config = config;
    }

    public @Nullable YamlDocument getConfig() {
        return this.config;
    }

    public void setConfigPath(String configPath) {
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
    public void setLocation(Location location) {
        this.location = location;
        this.worldRef = new WeakReference<>(location.getWorld());
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Attempts to get a location from this context.
     * <p>
     * If {@link #getLocation()} is null, it will try to get the player's location.
     */
    public @Nullable Location getHookOrPlayerLocation() {
        if (location != null) {
            return location;
        }
        if (player != null) {
            return player.getLocation();
        }
        return null;
    }

    public @NotNull FishingType getFishingType() {
        return this.fishingType;
    }

    public void setFishingType(@NotNull FishingType fishingType) {
        this.fishingType = fishingType;
    }

}
