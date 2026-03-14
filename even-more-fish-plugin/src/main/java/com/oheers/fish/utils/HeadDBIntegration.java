package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HeadDBIntegration implements Listener {

    @EventHandler
    public void onHDBLoad(DatabaseLoadEvent event) {
        EvenMoreFish plugin = EvenMoreFish.getInstance();
        plugin.getDependencyManager().setHdbapi(new HeadDatabaseAPI());

        plugin.getLogger().info("Detected that HeadDatabase has finished loading all items...");
        plugin.getLogger().info("Reloading EMF.");
        // We need to reload the plugin when head database is loaded.
        plugin.reload(null);
    }

}
