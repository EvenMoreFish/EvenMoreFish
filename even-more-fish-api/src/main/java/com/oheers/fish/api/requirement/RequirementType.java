package com.oheers.fish.api.requirement;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A way to check if a player meets a certain requirement.
 * This interface can be implemented by third party plugins to register their own Requirement.
 */
public abstract class RequirementType {

    private static final Map<String, RequirementType> loadedTypes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static Map<String, RequirementType> getLoadedTypes() {
        return Map.copyOf(loadedTypes);
    }

    public static void unregisterAll() {
        loadedTypes.clear();
    }

    public static @Nullable RequirementType get(@NotNull String identifier) {
        return loadedTypes.get(identifier);
    }

    public static boolean unregister(@NotNull String identifier) {
        if (!loadedTypes.containsKey(identifier)) {
            return false;
        }
        loadedTypes.remove(identifier);
        return true;
    }

    public RequirementType() {}

    /**
     * Checks if a player meets this requirement.
     * @param context The context to check
     * @param values The values to check this context against
     */
    public abstract boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values);

    /**
     * The identifier for this Requirement
     * @return The identifier for this Requirement
     */
    public abstract @NotNull String getIdentifier();

    public abstract @NotNull String getAuthor();

    public abstract @NotNull Plugin getPlugin();

    public boolean register() {
        if (loadedTypes.containsKey(getIdentifier())) {
            return false;
        }
        loadedTypes.put(getIdentifier(), this);
        EMFPlugin.getInstance().debug("Registered " + getIdentifier() + " RequirementType");
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
        if (!(obj instanceof RequirementType type)) {
            return false;
        }
        return getIdentifier().equals(type.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

}
