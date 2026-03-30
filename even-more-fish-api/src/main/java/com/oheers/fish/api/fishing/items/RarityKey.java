package com.oheers.fish.api.fishing.items;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

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
     * @param fish The fish this key belongs to.
     */
    public static @NotNull RarityKey of(@NotNull IFish fish) {
        return new RarityKey(fish.getRarity(), fish);
    }

    /**
     * Creates a RarityKey from a rarity name and a fish name.
     * @param rarityStr The name of the rarity.
     * @param fishStr The name of the fish.
     * @throws IllegalArgumentException If the rarity does not exist.
     * @throws IllegalArgumentException If the fish does not exist in the given rarity.
     */
    public static @NotNull RarityKey of(@NotNull String rarityStr, @NotNull String fishStr) throws IllegalArgumentException {
        IRarity rarity = AbstractFishManager.getInstance().getRarity(rarityStr);
        if (rarity == null) {
            throw new IllegalArgumentException("There is no rarity named " + rarityStr);
        }
        IFish fish = rarity.getFish(fishStr);
        if (fish == null) {
            throw new IllegalArgumentException("Rarity " + rarityStr + " has no fish named " + fishStr);
        }
        return new RarityKey(rarity, fish);
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
     * @return A {@link NamespacedKey} with the rarity ID as the namespace and the fish name as the key.
     */
    public @NotNull NamespacedKey toNamespacedKey() {
        return new NamespacedKey(this.rarity.getId(), this.fish.getName());
    }

}
