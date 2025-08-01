package com.oheers.evenmorefish.addons;


import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderItemAddon extends ItemAddon {

    @Override
    public String getIdentifier() {
        return "itemsadder";
    }

    @Override
    public String getPluginName() {
        return "ItemsAdder";
    }

    @Override
    public String getAuthor() {
        return "sarhatabaot";
    }

    @Override
    public String getVersion() {
        return "1.0.1";
    }

    @Override
    public ItemStack getItemStack(@NotNull String id) {
        String[] splitMaterialValue = id.split(":");
        if (!verifyItemsFormat(splitMaterialValue)) {
            getLogger().severe(() -> String.format(
                "Incorrect format for ItemsAdderItemAddon, use %s:namespace:id. Got %s",
                getIdentifier(),
                id)
            );
            return null;
        }

        final String namespaceId = splitMaterialValue[0] + ":" + splitMaterialValue[1];

        final CustomStack customStack = CustomStack.getInstance(namespaceId);
        if (customStack == null) {
            getLogger().info(() -> String.format("Could not obtain itemsadder item %s", namespaceId));
            return null;
        }
        return CustomStack.getInstance(namespaceId).getItemStack();
    }

    @EventHandler
    public void onItemsLoad(ItemsAdderLoadDataEvent event) {
        getLogger().info("Detected that ItemsAdder has finished loading all items...");
        getLogger().info("Reloading EMF.");

        EMFPlugin.getInstance().reload(null);
    }

    /**
     * Verifies the format of the given item id for the ItemsAdderItemAddon.
     *
     * @param splitMaterialValue The item id to verify.
     * @return {@code true} if the id is in the correct format, {@code false} otherwise.
     * The expected format is: "itemsadder:namespace:id".
     * This method is used to ensure that the item id provided by the user is in the correct format
     * before attempting to retrieve the corresponding ItemStack from ItemsAdder.
     */
    public boolean verifyItemsFormat(final String[] splitMaterialValue) {
        return splitMaterialValue.length == 2;
    }

}
