package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerLoadListener implements Listener {

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        HandlerList.unregisterAll(this);
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }
        EvenMoreFish plugin = EvenMoreFish.getInstance();

        // If Vault is disabled, we don't need this safety check.
        if (!plugin.getDependencyManager().isUsingVault()) {
            return;
        }
        // If economy is already enabled, we don't need to attempt to load it again.
        if (Economy.getInstance().isEnabled()) {
            return;
        }

        Logging.info("Vault economy failed to load during startup. Attempting to load again...");
        EvenMoreFish.getInstance().getDependencyManager().loadEconomy();
        if (!Economy.getInstance().isEnabled()) {
            Logging.warn("Safety check failed to hook into economy.");
        }
    }

}
