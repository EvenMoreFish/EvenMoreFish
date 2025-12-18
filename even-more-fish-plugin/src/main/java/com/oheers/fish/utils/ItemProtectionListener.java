package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.events.FishEatEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ItemProtectionListener implements Listener {

    // Protect against crafting with an EMF item.
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!MainConfig.getInstance().blockCrafting()) {
            return;
        }
        for (ItemStack craftItem : event.getInventory().getMatrix()) {
            if (craftItem == null) {
                continue;
            }
            if (FishUtils.isFish(craftItem) || FishUtils.isBaitObject(craftItem)) {
                event.setCancelled(true);
            }
        }
    }

    // Protect against consuming an EMF item.
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        // If the fish has eat-event, ignore item protection.
        if (FishEatEvent.getInstance().checkEatEvent(event) || !MainConfig.getInstance().blockConsume()) {
            return;
        }
        ItemStack item = event.getItem();
        if (FishUtils.isFish(item) || FishUtils.isBaitObject(item)) {
            event.setCancelled(true);
        }
    }

    // Protect against burning an EMF item as furnace fuel.
    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        if (!MainConfig.getInstance().blockFurnaceBurn()) {
            return;
        }
        ItemStack item = event.getFuel();
        if (FishUtils.isFish(item) || FishUtils.isBaitObject(item)) {
            event.setCancelled(true);
        }
    }

    // Protect against cooking an EMF item.
    @EventHandler
    public void onCook(BlockCookEvent event) {
        if (!MainConfig.getInstance().blockCooking()) {
            return;
        }
        ItemStack item = event.getSource();
        if (FishUtils.isFish(item) || FishUtils.isBaitObject(item)) {
            event.setCancelled(true);
        }
    }

    // Protect against placing an EMF item.
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!MainConfig.getInstance().blockPlacing()) {
            return;
        }
        ItemStack item = event.getItemInHand();
        // We can allow player heads through as we have the SkullSaver class.
        if (item.getType().equals(Material.PLAYER_HEAD)) {
            return;
        }
        if (FishUtils.isFish(item) || FishUtils.isBaitObject(item)) {
            event.setCancelled(true);
        }
    }

}
