package com.oheers.fish.api.economy;

import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A way to register custom economies for EMF.
 */
public interface EconomyType extends RegistryItem {

    String getIdentifier();

    @Override
    default @NotNull String getKey() {
        return getIdentifier();
    }

    double getMultiplier();

    boolean deposit(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier);

    boolean withdraw(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier);

    boolean has(@NotNull OfflinePlayer player, double amount);

    double get(@NotNull OfflinePlayer player);

    /**
     * Prepares a double for use with this economy type.
     * @param value The value to prepare.
     * @param applyMultiplier Should we apply the multiplier?
     * @return A prepared double for use with this economy type.
     */
    double prepareValue(double value, boolean applyMultiplier);

    /**
     * Creates a String to represent this value.
     * @param totalWorth The value to represent.
     * @param applyMultiplier Should the multiplier be applied to the value?
     * @return A String to represent this value.
     */
    @Nullable Component formatWorth(double totalWorth, boolean applyMultiplier);

    boolean isAvailable();

    default boolean register() {
        return EMFRegistry.ECONOMY_TYPE.register(this);
    }

}
