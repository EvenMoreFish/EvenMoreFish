package com.oheers.fish.api.requirement;

import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A way to check if a player meets a certain requirement.
 */
public abstract class RequirementType implements RegistryItem {

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

    @Override
    public @NotNull String getKey() {
        return getIdentifier();
    }

    public abstract @NotNull String getAuthor();

    public abstract @NotNull Plugin getPlugin();

    public boolean register() {
        return EMFRegistry.REQUIREMENT_TYPE.register(this);
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

    // Deprecated

    /**
     * @deprecated Use {@link RequirementTypeRegistry#getRegistry()} instead.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static Map<String, RequirementType> getLoadedTypes() {
        return EMFRegistry.REQUIREMENT_TYPE.getRegistry();
    }

    /**
     * @deprecated This method now does nothing as clearing the registry is unsupported.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static void unregisterAll() {}

    /**
     * @deprecated Use {@link RequirementTypeRegistry#get(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static @Nullable RequirementType get(@NotNull String identifier) {
        return EMFRegistry.REQUIREMENT_TYPE.get(identifier);
    }

    /**
     * @deprecated Use {@link RequirementTypeRegistry#unregister(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "2.1.0")
    public static boolean unregister(@NotNull String identifier) {
        return EMFRegistry.REQUIREMENT_TYPE.unregister(identifier);
    }

}
