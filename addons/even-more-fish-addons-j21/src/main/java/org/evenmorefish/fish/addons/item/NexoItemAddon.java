package org.evenmorefish.fish.addons.item;


import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class NexoItemAddon extends ItemAddon {
    
    @Override
    public String getIdentifier() {
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

    @Override
    public @Nullable String convertToString(@NonNull ItemStack item) {
        String id = NexoItems.idFromItem(item);
        if (id == null) {
            return null;
        }
        return "nexo:" + id;
    }

    @EventHandler
    public void onItemsLoad(NexoItemsLoadedEvent event) {
        getLogger().info("Detected that Nexo has finished loading all items...");
        getLogger().info("Reloading EMF.");

        EMFPlugin.getInstance().reload(null);
    }

}
