package com.oheers.fish.selling;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @deprecated Use {@link com.oheers.fish.api.economy.selling.SellHelper} instead.
 */
@Deprecated(forRemoval = true)
public class SellHelper {

    private final Player player;
    private final Inventory inventory;
    private final ItemStack[] array;

    public SellHelper(@NonNull Inventory inventory, @NonNull Player player) {
        this.inventory = inventory;
        this.player = player;
        this.array = null;
    }

    public SellHelper(@NonNull Inventory inventory, @NonNull Player player, boolean removeFromInventory) {
        this(inventory, player);
    }

    public SellHelper(@Nullable ItemStack @NonNull[] itemStacks, @NonNull Player player, boolean removeStacks) {
        this.inventory = null;
        this.player = player;
        this.array = itemStacks;
    }

   public SellHelper(@Nullable ItemStack @NonNull[] itemStacks, @NonNull Player player) {
       this(itemStacks, player, true);
   }

   /**
    * @deprecated use {@link #sell()} instead
    */
    @Deprecated(since = "2.1.4", forRemoval = true)
    public void sellFish() {
        sell();
    }

    public void sell() {
        if (inventory != null) {
            com.oheers.fish.api.economy.selling.SellHelper.get().sell(inventory, player);
        } else if (array != null) {
            com.oheers.fish.api.economy.selling.SellHelper.get().sell(array, player);
        } else {
            throw new IllegalStateException("Deprecated SellHelper has nothing to sell.");
        }
    }

}
