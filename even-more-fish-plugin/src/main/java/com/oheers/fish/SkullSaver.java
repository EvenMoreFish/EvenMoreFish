package com.oheers.fish;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class SkullSaver implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !event.isDropItems()) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        Block block = event.getBlock();

        if (!(block.getState(false) instanceof Skull skull)) return;
        if (block.getDrops().isEmpty()) return;

        IFish f = FishManager.getInstance().getFish(skull, event.getPlayer());
        if (f == null) {
            return;
        }

        event.setCancelled(true);
        event.setDropItems(false);

        ItemStack stack = block.getDrops().iterator().next().clone();
        ItemStack fishItem = f.give();
        stack.setItemMeta(fishItem.getItemMeta());
        block.setType(Material.AIR);
        block.getWorld().dropItem(block.getLocation(), stack);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        ItemStack stack = event.getItemInHand();

        if (stack.isEmpty()) {
            return;
        }

        IFish fish = FishManager.getInstance().getFish(stack);
        if (fish == null) {
            return;
        }

        if (block.getState(false) instanceof Skull sm) {
            FishManager.getInstance().setFishNbt(sm, fish);
            sm.update();
        } else {
            event.setCancelled(true);
        }
    }
    
}
