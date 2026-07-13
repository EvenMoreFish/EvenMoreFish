package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.events.EMFFishPreSaleEvent;
import com.oheers.fish.api.events.EMFFishSoldEvent;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SellHelper {

    private final @NotNull Player player;
    private final @NotNull Inventory inventory;
    private final @NotNull Economy economy;

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player) {
        this(inventory, player, Economy.getInstance());
    }

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player, @NotNull Economy economy) {
        this.player = player;
        this.inventory = inventory;
        this.economy = economy;
    }

    public void sell() {
        if (!economy.isEnabled()) {
            EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
            return;
        }

        boolean soldAny = false;
        double totalValue = 0;
        int count = 0;

        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.isEmpty()) {
                continue;
            }
            SoldFish sold = SoldFish.get(player, item);
            if (sold == null) {
                continue;
            }
            // PreSaleEvent - Allows for modifying the sold fish and cancellation.
            boolean cancelled = !new EMFFishPreSaleEvent(player, sold).callEvent();
            if (cancelled || sold.getValue() < 0) {
                continue;
            }

            logSoldFish(sold);
            sold.getFish().getSellRewards().forEach(reward -> reward.rewardPlayer(player, player.getLocation()));
            new EMFFishSoldEvent(player, sold).callEvent();

            inventory.setItem(slot, null);
            soldAny = true;
            totalValue += sold.getFinalValue();
            count += sold.getQuantity();
        }

        if (!soldAny) {
            EMFPlugin.getInstance().sendMessage("NO_SELLABLE_FISH", player);
            return;
        }

        // Give money
        economy.deposit(player, totalValue, true);

        // Send Message
        EMFPlugin.getInstance().sendSoldMessage(totalValue, count, player);

        // Play sound
        player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);
    }

    private void logSoldFish(@NotNull SoldFish soldFish) {
        EMFPlugin.getInstance().logSoldFish(soldFish);
    }

}
