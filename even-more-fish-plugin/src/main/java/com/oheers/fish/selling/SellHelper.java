package com.oheers.fish.selling;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link com.oheers.fish.api.economy.selling.SellHelper} instead.
 */
@Deprecated(forRemoval = true)
public class SellHelper {

    private final com.oheers.fish.api.economy.selling.SellHelper ref;

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player) {
        this.ref = new com.oheers.fish.api.economy.selling.SellHelper(inventory, player);
    }

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player, boolean removeFromInventory) {
        this(inventory, player);
    }

    /* TODO revisit.
    public SellHelper(@Nullable ItemStack @NotNull[] itemStacks, @NotNull Player player, boolean removeStacks) {
        this.ref = new com.oheers.fish.api.economy.selling.SellHelper(inventory, player);
    }
     */

    /* TODO revisit.
   public SellHelper(@Nullable ItemStack @NotNull[] itemStacks, @NotNull Player player) {
       this(itemStacks, player, true);
   */

   /**
    * @deprecated use {@link #sell()} instead
    */
    @Deprecated(since = "2.1.4", forRemoval = true)
    public void sellFish() {
        sell();
    }

    public void sell() {
        ref.sell();
    }

}
