package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class ItemAddonRegistry implements EMFRegistry<ItemAddon> {

    private static final ItemAddonRegistry instance = new ItemAddonRegistry();

    private final Map<String, ItemAddon> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private ItemAddonRegistry() {}

    public static @NonNull ItemAddonRegistry getInstance() {
        return instance;
    }

    @Override
    public boolean isEmpty() {
        return registry.isEmpty();
    }

    @Override
    public void clear() {
        registry.clear();
    }

    /**
     * @return An immutable copy of the current registry.
     */
    @Override
    public @NonNull Map<String, ItemAddon> getRegistry() {
        return Map.copyOf(registry);
    }

    /**
     * Get a value from the registry.
     *
     * @param key The key to look for.
     * @return The value, or null if not found.
     */
    @Override
    public @Nullable ItemAddon get(@NonNull String key) {
        return registry.get(key);
    }

    public @Nullable ItemStack getItem(final @NonNull String prefix, final @NonNull String id) {
        ItemAddon addon = registry.get(prefix);
        if (addon == null) {
            return null;
        }
        return addon.getItemStack(id);
    }

    /**
     * Get a value from the registry, or a default value if not found.
     *
     * @param key          The key to look for.
     * @param defaultValue The default value to return if not found.
     * @return The value, or the default value if not found.
     */
    @Override
    public @NonNull ItemAddon getOrDefault(@NonNull String key, @NonNull ItemAddon defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    /**
     * Unregister a key from the registry.
     *
     * @param key The key to unregister.
     * @return True if the key was unregistered, false if not found.
     */
    @Override
    public boolean unregister(@NonNull String key) {
        ItemAddon value = registry.remove(key);
        if (value == null) {
            return false;
        }
        HandlerList.unregisterAll(value);
        return true;
    }

    /**
     * Register a value in the registry.
     *
     * @param value The value to register.
     * @param force Whether to force the registration, overwriting any existing value.
     * @return True if the value was registered, false if a value with the same key already exists and force is false.
     */
    @Override
    public boolean register(@NonNull ItemAddon value, boolean force) {
        if (!value.canLoad()) {
            return false;
        }
        if (!force && registry.containsKey(value.getKey())) {
            return false;
        }
        registry.put(value.getKey(), value);
        Bukkit.getPluginManager().registerEvents(value, EMFPlugin.getInstance());
        EMFPlugin.getInstance().debug("Registered " + value.getKey() + " ItemAddon");
        return true;
    }
    
}
