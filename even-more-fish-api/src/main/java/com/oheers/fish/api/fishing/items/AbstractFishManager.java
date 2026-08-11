package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.AbstractFileBasedManager;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.TreeMap;

public abstract class AbstractFishManager<T extends IRarity> extends AbstractFileBasedManager<T> {

    private static AbstractFishManager<? extends IRarity> instance;

    protected AbstractFishManager() {
        if (instance != null) {
            throw new IllegalStateException("FishManager has already been initialized!");
        }
        instance = this;
    }

    public static @NonNull AbstractFishManager<? extends IRarity> getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FishManager has not been initialized yet!");
        }
        return instance;
    }

    public abstract @Nullable IRarity getRarity(@NonNull String rarityName);

    public abstract @Nullable IFish getFish(@NonNull String rarityName, @NonNull String fishName);

    public abstract @Nullable IFish getFish(@Nullable ItemStack item);

    public abstract @Nullable IFish getFish(@Nullable Skull skull, @Nullable Player fisher);

    public abstract @Nullable IFish getFish(@Nullable Entity itemEntity);

    public abstract boolean isFish(@Nullable ItemStack item);

    public abstract boolean isFish(@Nullable Skull skull);

    public abstract boolean isFish(@Nullable Entity itemEntity);

    /**
     * Applies fish NBT to the given item.
     */
    public abstract void setFishNbt(@NonNull ItemStack item, @NonNull IFish fish);

    /**
     * Applies fish NBT to the given skull block.
     */
    public abstract void setFishNbt(@NonNull Skull skull, @NonNull IFish fish);

    public abstract @NonNull TreeMap<String, ? extends IRarity> getRarityMap();

}
