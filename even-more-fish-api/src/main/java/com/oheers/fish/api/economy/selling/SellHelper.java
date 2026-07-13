package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.events.EMFFishSoldEvent;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

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
        if (!Economy.getInstance().isEnabled()) {
            EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
            return;
        }

        Iterator<ItemStack> iterator = inventory.iterator();
        boolean soldAny = false;
        double totalValue = 0;
        int count = 0;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            SoldFish sold = SoldFish.get(player, item);
            if (sold == null) {
                continue;
            }
            handleSale(sold);
            iterator.remove();

            soldAny = true;
            totalValue += sold.getValue();
            count += sold.getQuantity();
        }

        if (!soldAny) {
            EMFPlugin.getInstance().sendMessage("NO_SELLABLE_FISH", player);
            return;
        }

        // Give money
        Economy.getInstance().deposit(player, totalValue, true);

        // Send Message
        EMFPlugin.getInstance().sendSoldMessage(totalValue, count, player);

        // Play sound
        player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);
    }

    private void handleSale(@NotNull SoldFish sold) {
        new EMFFishSoldEvent(player, sold).callEvent(); // SoldFish can be edited during this event.

        sold.getFish().getSellRewards().forEach(reward -> reward.rewardPlayer(player, player.getLocation()));

        logSoldFish(sold);
    }

    private void logSoldFish(@NotNull SoldFish soldFish) {
        EMFPlugin.getInstance().logSoldFish(soldFish);
    }

}
