package com.oheers.fish.api.reward;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public abstract class RewardType {

    private static final Map<String, RewardType> loadedTypes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static Map<String, RewardType> getLoadedTypes() {
        return Map.copyOf(loadedTypes);
    }

    public static void unregisterAll() {
        loadedTypes.clear();
    }

    public static @Nullable RewardType get(@NotNull String identifier) {
        return loadedTypes.get(identifier);
    }

    public static boolean unregister(@NotNull String identifier) {
        if (!loadedTypes.containsKey(identifier)) {
            return false;
        }
        loadedTypes.remove(identifier);
        return true;
    }

    public RewardType() {}

    public abstract void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation);

    public abstract @NotNull String getIdentifier();

    public abstract @NotNull String getAuthor();

    public abstract @NotNull Plugin getPlugin();

    public boolean register() {
        if (loadedTypes.containsKey(getIdentifier())) {
            return false;
        }
        loadedTypes.put(getIdentifier(), this);
        EMFPlugin.getInstance().debug("Registered " + getIdentifier() + " RewardType");
        return true;
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

}