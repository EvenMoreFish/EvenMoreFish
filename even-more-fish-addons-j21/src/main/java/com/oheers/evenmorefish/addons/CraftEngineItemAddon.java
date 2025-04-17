package com.oheers.evenmorefish.addons;

import com.oheers.fish.api.addons.ItemAddon;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CraftEngineItemAddon extends ItemAddon {

    @Override
    public String getIdentifier() {
        return "craftengine";
    }

    @Override
    public String getPluginName() {
        return "CraftEngine";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public ItemStack getItemStack(String id) {
        Optional<CustomItem<ItemStack>> optionalItem = BukkitCraftEngine.instance().itemManager().getCustomItem(Key.of(id));
        if (optionalItem.isEmpty()) {
            getLogger().warning(() -> "CraftEngine item with id %s doesn't exist.".formatted(id));
            return null;
        }

        final ItemStack item = optionalItem.get().buildItemStack();

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain CraftEngine item %s", id));
            return null;
        }

        return item;
    }

}
