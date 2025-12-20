package com.oheers.fish.api.economy;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class EconomyTypeRegistry implements EMFRegistry<EconomyType> {

    private static final EconomyTypeRegistry instance = new EconomyTypeRegistry();

    private final Map<String, EconomyType> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private EconomyTypeRegistry() {}

    public static @NotNull EconomyTypeRegistry getInstance() {
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
    public @NotNull Map<String, EconomyType> getRegistry() {
        return Map.copyOf(registry);
    }

    /**
     * Get a value from the registry.
     *
     * @param key The key to look for.
     * @return The value, or null if not found.
     */
    @Override
    public @Nullable EconomyType get(@NotNull String key) {
        return registry.get(key);
    }

    /**
     * Get a value from the registry, or a default value if not found.
     *
     * @param key          The key to look for.
     * @param defaultValue The default value to return if not found.
     * @return The value, or the default value if not found.
     */
    @Override
    public @NotNull EconomyType getOrDefault(@NotNull String key, @NotNull EconomyType defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    /**
     * Unregister a key from the registry.
     *
     * @param key The key to unregister.
     * @return True if the key was unregistered, false if not found.
     */
    @Override
    public boolean unregister(@NotNull String key) {
        return registry.remove(key) != null;
    }

    /**
     * Register a value in the registry.
     *
     * @param value The value to register.
     * @param force Whether to force the registration, overwriting any existing value.
     * @return True if the value was registered, false if a value with the same key already exists and force is false.
     */
    @Override
    public boolean register(@NotNull EconomyType value, boolean force) {
        if (!force && registry.containsKey(value.getKey())) {
            return false;
        }
        registry.put(value.getKey(), value);
        EMFPlugin.getInstance().debug("Registered " + value.getKey() + " EconomyType");
        return true;
    }

}
