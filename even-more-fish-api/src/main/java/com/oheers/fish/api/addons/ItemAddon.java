package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemAddon implements Addon {

    private static final Map<String, ItemAddon> loaded = new HashMap<>();

    public static @Nullable ItemAddon get(final @NotNull String prefix) {
        return loaded.get(prefix);
    }

    public static @Nullable ItemStack getItem(final @NotNull String prefix, final @NotNull String id) {
        ItemAddon addon = loaded.get(prefix);
        if (addon == null) {
            return null;
        }
        return addon.getItemStack(id);
    }

    /**
     * @param id id of the ItemStack without the prefix.
     * @return The ItemStack via the id
     */
    public abstract ItemStack getItemStack(final String id);

    public abstract String getPluginName();

    @Override
    public boolean canLoad() {
        return getPluginName() == null || Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    public boolean register() {
        if (!canLoad()) {
            return false;
        }
        String id = getIdentifier();
        if (loaded.containsKey(id)) {
            return false;
        }
        loaded.put(id, this);
        EMFPlugin.getInstance().getLogger().info("Loaded " + getIdentifier() + " ItemAddon.");
        return true;
    }

    @Override
    public final String toString() {
        return String.format("ItemAddon[prefix: %s, author: %s]", getIdentifier(), getAuthor());
    }

}
