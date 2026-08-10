package com.oheers.fish.api.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * For internal use only.
 * <p>
 * This class is currently intended for internal use, and may be changed or removed without notice.
 */
@ApiStatus.Internal
public abstract class AbstractItemFactory {

    public abstract void setRandomIndex(int randomIndex);

    public abstract int getRandomIndex();

    public abstract @NonNull AbstractItemFactory createCopy();

    // Creation methods

    /**
     * Creates an ItemStack from this factory.
     */
    public @NonNull ItemStack createItem() {
        return createItem((Map<String, ?>) null);
    }

    /**
     * Creates an ItemStack from this factory and applies the given replacements, if provided.
     */
    public abstract @NonNull ItemStack createItem(@Nullable Map<String, ?> replacements);

    /**
     * Creates an ItemStack from this factory using the given player UUID.
     */
    public @NonNull ItemStack createItem(@NonNull UUID relevantPlayer) {
        return createItem(relevantPlayer, null);
    }

    /**
     * Creates an ItemStack from this factory using the given player UUID, and applies the given replacements, if provided.
     */
    public abstract @NonNull ItemStack createItem(@NonNull UUID relevantPlayer, @Nullable Map<String, ?> replacements);

}
