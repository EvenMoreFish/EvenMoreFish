package org.evenmorefish.fish.addons.item;

import com.oheers.fish.api.addons.ItemAddon;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItemFinder;
import com.willfp.ecoitems.items.EcoItems;
import com.willfp.ecoitems.items.ItemUtilsKt;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class EcoItemsItemAddon extends ItemAddon {

    @Override
    public String getPluginName() {
        return "EcoItems";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public String getIdentifier() {
        return "ecoitems";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ItemStack getItemStack(String id) {
        final EcoItem item = EcoItems.INSTANCE.getByID(id);

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain EcoItems item %s", id));
            return null;
        }

        return item.getItemStack();
    }

    @Override
    public @Nullable String convertToString(@NonNull ItemStack item) {
        EcoItem ecoItem = ItemUtilsKt.getEcoItem(item);
        if (ecoItem == null) {
            return null;
        }
        return "ecoitems:" + ecoItem.getID().toLowerCase(Locale.ROOT);
    }

}
