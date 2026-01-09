package com.oheers.fish.events;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FishInteractEvent implements Listener {

    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        // If the player is sneaking (to place the head), is using the offhand, or isn't clicking air, don't do anything
        if (event.getPlayer().isSneaking()
            || EquipmentSlot.OFF_HAND.equals(event.getHand())
            || !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }
        // Creates a replica of the fish we can use. If the item is null or isn't a fish, the null check will pass.
        Fish fish = FishUtils.getFish(event.getItem());
        if (fish == null) {
            return;
        }
        if (fish.hasIntRewards()) {
            // Cancel the interact event
            event.setCancelled(true);
            // Take one item from the player's event hand itemstack so we know that it's gone
            ItemStack itemInHand = event.getItem();
            event.getPlayer().getInventory().getItemInMainHand().setAmount(itemInHand.getAmount() - 1);
            // Runs through each eat-event
            fish.getActionRewards().forEach(r -> r.rewardPlayer(event.getPlayer(), null));
        }
    }
}
