package org.evenmorefish.fish.addons.item;

import com.oheers.fish.api.addons.ItemAddon;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.inventory.ItemStack;

public class MMOItemsItemAddon extends ItemAddon {

    @Override
    public ItemStack getItemStack(String id) {
        String[] splitMaterialValue = id.split(":");
        if (splitMaterialValue.length != 2) {
            getLogger().severe(() -> String.format(
                "Incorrect format for MMOItemsItemAddon, use %s:type:id. Got %s",
                getIdentifier(),
                id
            ));
            return null;
        }

        MMOItems plugin = MMOItems.plugin;
        Type type = plugin.getTypes().get(splitMaterialValue[0]);
        if (type == null) {
            getLogger().info(() -> String.format("Could not obtain MMOItems item %s", id));
            return null;
        }
        MMOItem item = plugin.getMMOItem(type, splitMaterialValue[1]);
        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain MMOItems item %s", id));
            return null;
        }

        return item.newBuilder().build();
    }

    @Override
    public String getPluginName() {
        return "MMOItems";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public String getIdentifier() {
        return "MMOITEMS";
    }

}
