package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.registry.EMFRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Deserializes an item from the given String.
 * <p>
 * Also checks item addons by default.
 */
public class ItemSerializer implements EMFSerializer<ItemStack> {

    private static final ItemSerializer INSTANCE = new ItemSerializer();

    private ItemSerializer() {}

    public static @NotNull ItemSerializer get() {
        return INSTANCE;
    }

    /**
     * @throws UnsupportedOperationException Items cannot reliably be serialized.
     */
    @Deprecated
    @Override
    public @NotNull String serialize(@NotNull ItemStack element) {
        throw new UnsupportedOperationException("ItemSerializer#serialize is unsupported.");
    }

    /**
     * Deserializes an ItemStack from the given String.
     * <p>
     * If an invalid material is provided, also checks registered item addons.
     */
    @Override
    public @Nullable ItemStack deserialize(@Nullable String element) {
        return deserialize(element, true);
    }

    /**
     * Deserializes an ItemStack from the given String.
     * <p>
     * Optionally checks for item addons.
     */
    public @Nullable ItemStack deserialize(@Nullable String element, boolean useItemAddons) {
        if (element == null) {
            return null;
        }
        if (useItemAddons && element.contains(":")) {
            return deserializeItemAddon(element);
        } else {
            return deserializeMaterial(element);
        }
    }

    public @Nullable ItemStack deserializeMaterial(@Nullable String element) {
        if (element == null) {
            return null;
        }
        try {
            Material material = Material.valueOf(element.toUpperCase(Locale.ROOT));
            return new ItemStack(material);
        } catch (IllegalArgumentException exception) {
            Logging.debug(element + " is not a valid material.");
            return null;
        }
    }

    public @Nullable ItemStack deserializeItemAddon(@Nullable String element) {
        if (element == null) {
            return null;
        }
        try {
            final String[] split = element.split(":", 2);
            final String prefix = split[0];
            final String id = split[1];
            Logging.debug("GET ITEM for Addon(%s) Id(%s)".formatted(prefix, id));
            return EMFRegistry.ITEM_ADDON.getItem(prefix, id);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return null;
        }
    }

}
