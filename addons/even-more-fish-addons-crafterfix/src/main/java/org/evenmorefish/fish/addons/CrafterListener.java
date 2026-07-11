package org.evenmorefish.fish.addons;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.block.Crafter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CrafterListener implements Listener {

    @EventHandler
    public void onCrafterCraft(CrafterCraftEvent event) {
        if (!MainConfig.getInstance().preventCrafting()) {
            return;
        }
        Crafter crafter = (Crafter) event.getBlock().getState();
        for (ItemStack craftItem : crafter.getInventory().getContents()) {
            if (craftItem == null) {
                continue;
            }
            if (FishManager.getInstance().isFish(craftItem) || FishUtils.isBaitObject(craftItem)) {
                event.setCancelled(true);
            }
        }
    }

}
