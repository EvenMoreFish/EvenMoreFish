package com.oheers.fish.recipe;

import com.oheers.fish.api.Logging;
import com.oheers.fish.fishing.rods.RodManager;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RecipeListener implements Listener {

    @EventHandler
    public void onReload(ServerResourcesReloadedEvent event) {
        Logging.info("Detected server reload. Reloading all custom recipes.");

        // Custom rods are currently the only items with custom recipes.
        RodManager.getInstance().reload();
    }

}
