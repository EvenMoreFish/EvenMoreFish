package org.evenmorefish.fish.addons.item;

import com.oheers.fish.api.addons.ItemAddon;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.ItemManager;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    public @Nullable String convertToString(@NonNull ItemStack item) {
        // The single worst API I've ever used. Why is there not a simple method to do this?
        NBTItem nbtItem = NBTItem.get(item);
        String type = nbtItem.getType();
        if (type == null) {
            return null;
        }
        String id = nbtItem.getString("MMOITEMS_ITEM_ID");
        if (id == null) {
            return null;
        }
        return "mmoitems:" + type + ":" + id;
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
