package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.logging.Logger;

/**
 * A way to add your own items into EMF's ItemFactory.
 */
public abstract class ItemAddon implements Listener, RegistryItem {

    /**
     * @param id id of the ItemStack without the prefix.
     * @return The ItemStack via the id
     */
    public abstract ItemStack getItemStack(final String id);

    public abstract String getPluginName();

    public abstract String getAuthor();

    public abstract String getIdentifier();

    @Override
    public @NotNull String getKey() {
        return getIdentifier();
    }

    public String getVersion() {
        return "0.0.0";
    }

    public boolean canLoad() {
        return getPluginName() == null || Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    public boolean register() {
        return EMFRegistry.ITEM_ADDON.register(this);
    }

    @Override
    public final String toString() {
        return String.format("ItemAddon[prefix: %s, author: %s]", getIdentifier(), getAuthor());
    }

    protected Logger getLogger() {
        return EMFPlugin.getInstance().getLogger();
    }

    // Deprecated

    @Deprecated(forRemoval = true, since = "2.1.0")
    public static Map<String, ItemAddon> getLoadedAddons() {
        return EMFRegistry.ITEM_ADDON.getRegistry();
    }

    /**
     * @deprecated This method now does nothing as clearing the registry is unsupported.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static void unregisterAll() {}

    @Deprecated(forRemoval = true, since = "2.1.0")
    public static @Nullable ItemAddon get(final @NotNull String prefix) {
        return EMFRegistry.ITEM_ADDON.get(prefix);
    }

    @Deprecated(forRemoval = true, since = "2.1.0")
    public static @Nullable ItemStack getItem(final @NotNull String prefix, final @NotNull String id) {
        return EMFRegistry.ITEM_ADDON.getItem(prefix, id);
    }

}
