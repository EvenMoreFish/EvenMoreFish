package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.AbstractFileBasedManager;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public abstract class AbstractFishManager<T extends IRarity> extends AbstractFileBasedManager<T> {

    private static AbstractFishManager<? extends IRarity> instance;

    protected AbstractFishManager() {
        if (instance != null) {
            throw new IllegalStateException("FishManager has already been initialized!");
        }
        instance = this;
    }

    public static @NotNull AbstractFishManager<? extends IRarity> getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FishManager has not been initialized yet!");
        }
        return instance;
    }

    public abstract @Nullable IRarity getRarity(@NotNull String rarityName);

    public abstract @Nullable IFish getFish(@NotNull String rarityName, @NotNull String fishName);

    public abstract @Nullable IFish getFish(@Nullable ItemStack item);

    public abstract @Nullable IFish getFish(@Nullable Skull skull, @Nullable Player fisher);

    public abstract @Nullable IFish getFish(@Nullable Entity itemEntity);

    public abstract boolean isFish(@Nullable ItemStack item);

    public abstract boolean isFish(@Nullable Skull skull);

    public abstract boolean isFish(@Nullable Entity itemEntity);

    public abstract @NotNull TreeMap<String, ? extends IRarity> getRarityMap();

}
