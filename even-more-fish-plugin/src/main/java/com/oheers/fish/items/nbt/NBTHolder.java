package com.oheers.fish.items.nbt;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public abstract class NBTHolder<T> {

    protected final T obj;

    protected boolean autoSave = true;

    public static @NotNull NBTHolder<ItemStack> itemStack(@NotNull ItemStack item) {
        return EvenMoreFish.getInstance().getVersionProvider().createItemStackNbtHolder(item);
    }

    public static @NotNull NBTHolder<BlockState> blockState(@NotNull BlockState state) {
        return EvenMoreFish.getInstance().getVersionProvider().createBlockStateNbtHolder(state);
    }

    public NBTHolder(@NotNull T obj) {
        this.obj = obj;
    }

    public abstract boolean hasKey(@NotNull NamespacedKey namespacedKey);

    public abstract @Nullable String getString(@NotNull NamespacedKey namespacedKey);

    /**
     * Sets a String in the specified NBT location. If null is passed, the key will be removed.
     */
    public abstract void setString(@NotNull NamespacedKey namespacedKey, @Nullable String value);

    public abstract @Nullable Float getFloat(@NotNull NamespacedKey namespacedKey);

    /**
     * Sets a Float in the specified NBT location. If null is passed, the key will be removed.
     */
    public abstract void setFloat(@NotNull NamespacedKey namespacedKey, @Nullable Float value);

    public abstract @Nullable Integer getInteger(@NotNull NamespacedKey namespacedKey);

    /**
     * Sets an Integer in the specified NBT location. If null is passed, the key will be removed.
     */
    public abstract void setInteger(@NotNull NamespacedKey namespacedKey, @Nullable Integer value);

    /**
     * Sets whether the object will be saved after each edit.
     * <p>
     * Set to false if doing bulk changes for better performance.
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public abstract void save();

}
