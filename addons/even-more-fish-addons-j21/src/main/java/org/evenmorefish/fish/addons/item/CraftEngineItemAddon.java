package org.evenmorefish.fish.addons.item;

import com.oheers.fish.api.addons.ItemAddon;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.inventory.ItemStack;

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
        return "2.0.0";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public ItemStack getItemStack(String id) {
        BukkitItemDefinition itemDef = CraftEngineItems.byId(id);
        if (itemDef == null) {
            getLogger().warning(() -> "CraftEngine item with id %s doesn't exist.".formatted(id));
            return null;
        }

        final ItemStack item = itemDef.buildBukkitItem();

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain CraftEngine item %s", id));
            return null;
        }

        return item;
    }

}
