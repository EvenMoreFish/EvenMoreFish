package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.Logging;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * An immutable key that uniquely identifies a fish by its rarity and name.
 */
public final class RarityKey {

    private final IRarity rarity;
    private final IFish fish;

    private RarityKey(IRarity rarity, IFish fish) {
        this.rarity = rarity;
        this.fish = fish;
    }

    /**
     * Creates a RarityKey from an IFish instance.
     *
     * @param fish The fish this key belongs to.
     */
    public static @NotNull RarityKey of(@NotNull IFish fish) {
        return new RarityKey(fish.getRarity(), fish);
    }

    /**
     * Creates a RarityKey from a rarity name and a fish name.
     *
     * @param rarityStr The name of the rarity.
     * @param fishStr   The name of the fish.
     * @return A valid RarityKey, or null if the rarity or fish do not exist.
     */
    public static @Nullable RarityKey of(@NotNull String rarityStr, @NotNull String fishStr) {
        IRarity rarity = AbstractFishManager.getInstance().getRarity(rarityStr);
        if (rarity == null) {
            Logging.warn("There is no rarity named " + rarityStr, new IllegalArgumentException());
            return null;
        }
        IFish fish = rarity.getFish(fishStr);
        if (fish == null) {
            fish = rarity.getFish(fishStr.replace("_", " "));
        }
        if (fish == null) {
            Logging.warn("Rarity " + rarityStr + " has no fish named " + fishStr, new IllegalArgumentException());
            return null;
        }
        return new RarityKey(rarity, fish);
    }

    /**
     * Creates a RarityKey from a key string. ("namespace:value")
     *
     * @param keyString The key string to use.
     */
    public static @Nullable RarityKey of(@NotNull String keyString) {
        String[] split = keyString.split(":");
        if (split.length < 2) {
            return null;
        }
        String rarity = split[0];
        String fish = split[1];
        return of(rarity, fish);
    }

    /**
     * @return A copy of the fish this key belongs to.
     */
    public @NotNull IFish getFish() {
        return this.fish.createCopy();
    }

    /**
     * @return The rarity this key belongs to.
     */
    public @NotNull IRarity getRarity() {
        return this.rarity;
    }

    /**
     * @return A String to represent this key.
     */
    public String toString() {
        return String.join(":",
            this.rarity.getId().toLowerCase(),
            this.fish.getName().toLowerCase().replace(" ", "_")
        );
    }

}
