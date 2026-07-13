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
        if (!economy.isEnabled()) {
            EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
            return;
        }

        Iterator<ItemStack> iterator = inventory.iterator();
        boolean soldAny = false;
        double totalValue = 0;
        int count = 0;
        while (iterator.hasNext()) {
            SoldFish sold = handleItem(iterator.next());
            if (sold == null || sold.getValue() < 0) {
                continue;
            }
            sold.getFish().getSellRewards().forEach(reward -> reward.rewardPlayer(player, player.getLocation()));

            iterator.remove();
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

    private SoldFish handleItem(@NotNull ItemStack item) {
        SoldFish sold = SoldFish.get(player, item);
        if (sold == null) {
            return null;
        }
        new EMFFishSoldEvent(player, sold).callEvent(); // SoldFish can be edited during this event.
        if (sold.getValue() < 0) {
            return null;
        }
        logSoldFish(sold);
        return sold;
    }

    private void logSoldFish(@NotNull SoldFish soldFish) {
        EMFPlugin.getInstance().logSoldFish(soldFish);
    }

}
