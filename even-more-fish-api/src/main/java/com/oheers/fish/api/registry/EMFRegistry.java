package com.oheers.fish.api.registry;

import com.oheers.fish.api.addons.ItemAddonRegistry;
import com.oheers.fish.api.boost.RarityBoostRegistry;
import com.oheers.fish.api.economy.EconomyTypeRegistry;
import com.oheers.fish.api.requirement.RequirementTypeRegistry;
import com.oheers.fish.api.reward.RewardTypeRegistry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface EMFRegistry<T extends RegistryItem> {

    @NonNull RequirementTypeRegistry REQUIREMENT_TYPE = RequirementTypeRegistry.getInstance();
    @NonNull RewardTypeRegistry REWARD_TYPE = RewardTypeRegistry.getInstance();
    @NonNull ItemAddonRegistry ITEM_ADDON = ItemAddonRegistry.getInstance();
    @NonNull EconomyTypeRegistry ECONOMY_TYPE = EconomyTypeRegistry.getInstance();
    @NonNull RarityBoostRegistry RARITY_BOOST = RarityBoostRegistry.getInstance();

    boolean isEmpty();

    void clear();

    /**
     * @return An immutable copy of the current registry.
     */
    @NonNull Map<String, T> getRegistry();

    /**
     * Get a value from the registry.
     * @param key The key to look for.
     * @return The value, or null if not found.
     */
    @Nullable T get(@NonNull String key);

    /**
     * Get a value from the registry, or a default value if not found.
     * @param key The key to look for.
     * @param defaultValue The default value to return if not found.
     * @return The value, or the default value if not found.
     */
    @NonNull T getOrDefault(@NonNull String key, @NonNull T defaultValue);

    /**
     * Unregister a key from the registry.
     * @param key The key to unregister.
     * @return True if the key was unregistered, false if not found.
     */
    boolean unregister(@NonNull String key);

    /**
     * Unregister a value from the registry.
     * @param value The value to unregister.
     * @return True if the value was unregistered, false if not found.
     */
    default boolean unregister(@NonNull T value) {
        return unregister(value.getKey());
    }

    /**
     * Register a value in the registry.
     * @param value The value to register.
     * @param force Whether to force the registration, overwriting any existing value.
     * @return True if the value was registered, false if a value with the same key already exists and force is false.
     */
    boolean register(@NonNull T value, boolean force);

    /**
     * Register a value in the registry.
     * @param value The value to register.
     * @return True if the value was registered, false if a value with the same key already exists.
     */
    default boolean register(@NonNull T value) {
        return register(value, false);
    }

}
