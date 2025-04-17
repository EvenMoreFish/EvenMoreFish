package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class ItemAddon implements Listener {

    private static final Map<String, ItemAddon> loaded = new HashMap<>();

    public static Map<String, ItemAddon> getLoadedAddons() {
        return Map.copyOf(loaded);
    }

    public static void unregisterAll() {
        loaded.clear();
    }

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

    public abstract String getAuthor();

    public abstract String getIdentifier();

    public String getVersion() {
        return "0.0.0";
    }

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
        Bukkit.getPluginManager().registerEvents(this, EMFPlugin.getInstance());
        loaded.put(id, this);
        EMFPlugin.getInstance().debug("Loaded " + getIdentifier() + " ItemAddon.");
        return true;
    }

    @Override
    public final String toString() {
        return String.format("ItemAddon[prefix: %s, author: %s]", getIdentifier(), getAuthor());
    }

    protected Logger getLogger() {
        return EMFPlugin.getInstance().getLogger();
    }

}
