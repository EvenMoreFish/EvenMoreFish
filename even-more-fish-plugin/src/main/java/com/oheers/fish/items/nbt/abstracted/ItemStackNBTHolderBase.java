package com.oheers.fish.items.nbt.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.utils.MinecraftVersionHelper;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

public abstract class ItemStackNBTHolderBase extends NBTHolder<ItemStack> {

    private static final Field CRAFT_DELEGATE;

    static {
        Field field;
        try {
            field = ItemStack.class.getDeclaredField("craftDelegate");
            field.setAccessible(true);
        // Should only throw on 1.20 servers.
        } catch (NoSuchFieldException exception) {
            field = null;
        }
        CRAFT_DELEGATE = field;
    }

    public ItemStackNBTHolderBase(@NonNull ItemStack obj) {
        super(obj);
    }

    public ItemStackNBTHolderBase(@NonNull String raw) {
        super(createRaw(raw));
    }

    /**
     * Creates an ItemStack from the raw string and throws an error if the string is invalid.
     */
    private static @NonNull ItemStack createRaw(@NonNull String raw) {
        ItemStack rawItem = EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(raw);
        if (rawItem == null) {
            throw new RuntimeException("Invalid raw NBT");
        }
        return rawItem;
    }

    protected static @Nullable ItemStack fetchCraftDelegate(@NonNull ItemStack item) {
        if (CRAFT_DELEGATE == null) {
            // Field does exist on 1.21.1+ so we need to log an error.
            if (MinecraftVersionHelper.isAtLeastVersion("1.21.1")) {
                Logging.error("Could not fetch craftDelegate method.");
            }
            return item;
        }
        try {
            return (ItemStack) CRAFT_DELEGATE.get(item);
        } catch (IllegalAccessException | IllegalArgumentException | ClassCastException exception) {
            Logging.error("Failed to fetch craftDelegate.", exception);
            return item;
        }
    }

}
