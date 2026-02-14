package com.oheers.fish.api.reward;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

/**
 * A way to reward a player for certain actions.
 */
public abstract class RewardType implements RegistryItem {

    public RewardType() {}

    public abstract void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation);

    public abstract @NotNull String getIdentifier();

    @Override
    public @NotNull String getKey() {
        return getIdentifier();
    }

    public abstract @NotNull String getAuthor();

    public abstract @NotNull Plugin getPlugin();

    public boolean register() {
        return EMFRegistry.REWARD_TYPE.register(this);
    }

    public boolean unregister() {
        return unregister(getIdentifier());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RewardType type)) {
            return false;
        }
        return getIdentifier().equals(type.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    // Deprecated

    /**
     * @deprecated Use {@link RewardTypeRegistry#getRegistry()} instead.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static Map<String, RewardType> getLoadedTypes() {
        return EMFRegistry.REWARD_TYPE.getRegistry();
    }

    /**
     * @deprecated This method now does nothing as clearing the registry is unsupported.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static void unregisterAll() {}

    /**
     * @deprecated Use {@link RewardTypeRegistry#get(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static @Nullable RewardType get(@NotNull String identifier) {
        return EMFRegistry.REWARD_TYPE.get(identifier);
    }

    /**
     * @deprecated Use {@link RewardTypeRegistry#unregister(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static boolean unregister(@NotNull String identifier) {
        return EMFRegistry.REWARD_TYPE.unregister(identifier);
    }

}