package com.oheers.fish.utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class AntiCraft implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        for (ItemStack craftItem : event.getInventory().getMatrix()) {
            if (craftItem == null) continue;
            if (FishUtils.isFish(craftItem) || FishUtils.isBaitObject(craftItem)) event.setCancelled(true);
        }
    }
}
