package com.oheers.fish.api;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EMFAPI {

    /**
     * @deprecated Use {@link com.oheers.fish.api.fishing.items.AbstractFishManager#isFish(ItemStack)} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean isFish(@Nullable ItemStack item) {
        return FishManager.getInstance().isFish(item);
    }

    /**
     * @deprecated Use {@link com.oheers.fish.api.fishing.items.AbstractFishManager#isFish(Skull)} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean isFish(@Nullable Skull skull) {
        return FishManager.getInstance().isFish(skull);
    }

    /**
     * @deprecated Use {@link com.oheers.fish.api.baits.AbstractBaitManager#isBait(ItemStack)} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean isBait(@NotNull ItemStack item) {
        return BaitManager.getInstance().isBait(item);
    }

    /**
     * @deprecated Use {@link com.oheers.fish.api.fishing.items.AbstractFishManager#getFish(ItemStack)} instead.
     */
    @Deprecated(forRemoval = true)
    public @Nullable Fish getFish(ItemStack item) {
        IFish abstracted = FishManager.getInstance().getFish(item);
        return (abstracted instanceof Fish fish) ? fish : null;
    }

    /**
     * @deprecated Use {@link com.oheers.fish.api.fishing.items.AbstractFishManager#getFish(Skull, Player)} instead.
     */
    @Deprecated(forRemoval = true)
    public @Nullable Fish getFish(Skull skull, Player fisher) throws InvalidFishException {
        IFish abstracted = FishManager.getInstance().getFish(skull, fisher);
        return (abstracted instanceof Fish fish) ? fish : null;
    }

    /**
     * @deprecated Use {@link com.oheers.fish.api.fishing.items.AbstractFishManager#getFish(String, String)} instead.
     */
    @Deprecated(forRemoval = true)
    public @Nullable Fish getFish(String rarityName, String fishName) {
        return FishManager.getInstance().getFish(rarityName, fishName);
    }

}
