package com.oheers.evenmorefish.addons;


import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.apache.commons.lang3.JavaVersion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class NexoItemAddon extends ItemAddon implements Listener {
    
    @Override
    public String getPrefix() {
        return "nexo";
    }

    @Override
    public String getPluginName() {
        return "Nexo";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public JavaVersion getRequiredJavaVersion() {
        return JavaVersion.JAVA_21;
    }

    @Override
    public ItemStack getItemStack(String id) {
        if (!NexoItems.exists(id)) {
            getLogger().warning(() -> "Nexo item with id %s doesn't exist.".formatted(id));
            return null;
        }

        final ItemBuilder item = NexoItems.itemFromId(id);

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain Nexo item %s", id));
            return null;
        }

        return item.build();
    }

    @EventHandler
    public void onItemsLoad(NexoItemsLoadedEvent event) {
        getLogger().info("Detected that Nexo has finished loading all items...");
        getLogger().info("Reloading EMF.");

        EMFPlugin.getInstance().reload(null);
    }

}
