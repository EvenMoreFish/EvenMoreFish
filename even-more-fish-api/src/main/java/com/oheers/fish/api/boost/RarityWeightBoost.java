package com.oheers.fish.api.boost;

import com.oheers.fish.api.registry.RegistryItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An external modifier consulted every time EvenMoreFish rolls a rarity for a catch.
 * <p>
 * Implementations are registered with the {@link RarityBoostRegistry} by other plugins
 * (for example area-of-effect fishing buffs) and return a multiplier that is applied to
 * a rarity's configured weight for a single roll. The {@link #getKey() key} should be
 * the registering plugin's name.
 */
public interface RarityWeightBoost extends RegistryItem {

    /**
     * Returns the multiplier to apply to a rarity's configured weight for one catch.
     * <p>
     * Called on the server thread for every candidate rarity of every catch, so
     * implementations must be fast and must not touch blocking IO.
     *
     * @param fisher   The player catching the fish.
     * @param location The location the fish is being caught at (the hook location).
     * @param rarityId The id of the candidate rarity, as configured in the rarities folder.
     * @return The weight multiplier; {@code 1.0} leaves the rarity unchanged, values above
     *         {@code 1.0} make it more likely, values between {@code 0.0} and {@code 1.0}
     *         make it less likely. Must not be negative.
     */
    double weightMultiplier(@NotNull Player fisher, @NotNull Location location, @NotNull String rarityId);

}
