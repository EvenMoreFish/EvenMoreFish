package com.oheers.fish.items.nbt;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public enum NbtKeys {
    EMF_FISH_PLAYER("emf-fish-player"),
    EMF_FISH_RARITY("emf-fish-rarity"),
    EMF_FISH_LENGTH("emf-fish-length"),
    EMF_FISH_NAME("emf-fish-name"),
    EMF_FISH_RANDOM_INDEX("emf-fish-random-index"),
    EMF_BAIT("emf-bait"),
    EMF_APPLIED_BAIT("emf-applied-bait"),
    EMF_BAIT_REFORMATTED("emf-bait-reformatted"),
    EMF_ROD_NBT("emf-rod-nbt"),
    EMF_ROD_ID("emf-rod-id");

    private final String value;

    NbtKeys(@NotNull String value) {
        this.value = value;
    }

    public @NotNull NamespacedKey get() {
        return new NamespacedKey(EvenMoreFish.getInstance(), this.value);
    }

    public @NotNull NamespacedKey get(@NotNull String namespace) {
        return new NamespacedKey(namespace, this.value);
    }

}
