package com.oheers.fish;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.selling.WorthNBT;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class SkullSaver implements Listener {
    
    // EventPriority.HIGHEST makes this run last so it can listen to the cancels of protection plugins like Towny
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        Block block = event.getBlock();

        if (!isHead(block)) return;
        if (block.getDrops().isEmpty()) return;

        BlockState state = event.getBlock().getState();
        Skull skullMeta = (Skull) state;
        if (!FishManager.getInstance().isFish(skullMeta)) return;

        ItemStack stack = block.getDrops().iterator().next().clone();
        event.setCancelled(true);
        event.setDropItems(false);

        IFish f = FishManager.getInstance().getFish(skullMeta, event.getPlayer());
        if (f == null) {
            // Uncancel the event so people can still pick up the heads.
            event.setCancelled(false);
            event.setDropItems(true);
            return;
        }
        ItemStack fishItem = f.give();
        stack.setItemMeta(fishItem.getItemMeta());
        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), stack);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BONE_BLOCK_BREAK, 1, 1);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Block block = event.getBlock();
        ItemStack stack = event.getItemInHand();
        
        if (stack.getAmount() == 0 || !stack.hasItemMeta()) {
            return;
        }
        
        if (FishManager.getInstance().isFish(stack)) {
            
            if (block.getState() instanceof Skull sm) {
                IFish iFish = FishManager.getInstance().getFish(stack);
                if (iFish instanceof Fish fish) {
                    WorthNBT.setNBT(sm, fish);
                    sm.update();
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
    
    private boolean isHead(final Block block) {
        return block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD;
    }
    
}
