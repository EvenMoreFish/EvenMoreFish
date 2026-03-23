package com.oheers.fish.baits.model;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This is returned when a bait has been applied to a fishing rod, the remaining cursor items is how many of the bait
 * were unsuccessfully applied, whether this was due to the fishing rod having no available slots, having maxed
 * out the slot for that bait.
 *
 * @param cursorItemModifier How many baits should remain on the cursor after the application.
 * @param fishingRod         The fishing rod with the updated baits.
 */
public record ApplicationResult(@NotNull ItemStack fishingRod, int cursorItemModifier) {

    /**
     * @return How many baits should remain on the cursor after the application.
     */
    @Override
    public int cursorItemModifier() {
        return cursorItemModifier;
    }

    /**
     * @return The fishing rod with the updated baits.
     */
    @Override
    public ItemStack fishingRod() {
        return fishingRod;
    }

}
