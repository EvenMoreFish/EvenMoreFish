package com.oheers.fish.api.boost;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

/**
 * Registry of external {@link RarityWeightBoost}s applied when EvenMoreFish rolls a rarity.
 * <p>
 * Other plugins register a boost on enable and unregister it on disable. When several
 * boosts are registered, their multipliers are combined by multiplication.
 */
public class RarityBoostRegistry implements EMFRegistry<RarityWeightBoost> {

    private static final RarityBoostRegistry instance = new RarityBoostRegistry();

    private final Map<String, RarityWeightBoost> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private RarityBoostRegistry() {}

    public static @NotNull RarityBoostRegistry getInstance() {
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
    public @NotNull Map<String, RarityWeightBoost> getRegistry() {
        return Map.copyOf(registry);
    }

    /**
     * Get a value from the registry.
     *
     * @param key The key to look for.
     * @return The value, or null if not found.
     */
    @Override
    public @Nullable RarityWeightBoost get(@NotNull String key) {
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
    public @NotNull RarityWeightBoost getOrDefault(@NotNull String key, @NotNull RarityWeightBoost defaultValue) {
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
    public boolean register(@NotNull RarityWeightBoost value, boolean force) {
        if (!force && registry.containsKey(value.getKey())) {
            return false;
        }
        registry.put(value.getKey(), value);
        EMFPlugin.getInstance().debug("Registered " + value.getKey() + " RarityWeightBoost");
        return true;
    }

    /**
     * The combined weight multiplier for one candidate rarity of one catch: the product of
     * every registered boost's multiplier. Negative results from misbehaving boosts are
     * clamped to zero so a bad registration can never produce negative weights.
     *
     * @param fisher   The player catching the fish.
     * @param location The location the fish is being caught at.
     * @param rarityId The id of the candidate rarity.
     * @return The combined multiplier, {@code 1.0} when no boosts are registered.
     */
    public double combinedMultiplier(@NotNull Player fisher, @NotNull Location location, @NotNull String rarityId) {
        double combined = 1.0;
        for (RarityWeightBoost boost : registry.values()) {
            combined *= boost.weightMultiplier(fisher, location, rarityId);
        }
        return Math.max(0.0, combined);
    }

}
