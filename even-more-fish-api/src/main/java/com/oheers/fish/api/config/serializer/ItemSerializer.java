package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.addons.ItemAddonRegistry;
import com.oheers.fish.api.registry.EMFRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Deserializes an item from the given String.
 * <p>
 * Also checks item addons by default.
 */
public class ItemSerializer implements EMFSerializer<ItemStack> {

    private static final ItemSerializer INSTANCE = new ItemSerializer();

    private ItemSerializer() {}

    public static @NonNull ItemSerializer get() {
        return INSTANCE;
    }

    /**
     * Serializes an ItemStack.
     * <p>
     * Checks all item addons, and falls back to material name if none match.
     */
    @Override
    public @NonNull String serialize(@NonNull ItemStack element) {
        return serialize(element, true);
    }

    /**
     * Deserializes an ItemStack.
     * <p>
     * Optionally checks for item addons.
     */
    public @NonNull String serialize(@NonNull ItemStack element, boolean useItemAddons) {
        if (useItemAddons) {
            for (ItemAddon addon : EMFRegistry.ITEM_ADDON.getRegistry().values()) {
                String string = addon.convertToString(element);
                if (string != null) {
                    return string;
                }
            }
        }
        return element.getType().toString();
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
