---
title: Creating Item Addons
sidebar_position: 2
---

## Getting Started
For reference here is the implementation of the ItemsAdder addon:

```java title="ItemsAdder Item Addon"
package org.evenmorefish.fish.addons.item;

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
    
    public boolean verifyItemsFormat(final String[] splitMaterialValue) {
        return splitMaterialValue.length == 2;
    }

}

```