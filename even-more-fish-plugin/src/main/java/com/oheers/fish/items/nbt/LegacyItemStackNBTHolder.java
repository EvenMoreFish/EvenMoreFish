package com.oheers.fish.items.nbt;

import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class LegacyItemStackNBTHolder extends NBTHolder<ItemStack> {

    private final ItemMeta meta;

    public LegacyItemStackNBTHolder(@NotNull ItemStack obj) {
        super(obj);
        this.meta = obj.getItemMeta();
    }

    @Override
    public boolean hasNamespace(@NotNull String namespace) {
        return getData().getKeys().stream()
            .anyMatch(key -> key.getNamespace().equals(namespace.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean hasKey(@NotNull NamespacedKey namespacedKey) {
        return getData().has(namespacedKey);
    }

    @Nullable
    @Override
    public String getString(@NotNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * Sets a String in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setString(@NotNull NamespacedKey namespacedKey, @Nullable String value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Nullable
    @Override
    public Float getFloat(@NotNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.FLOAT);
    }

    /**
     * Sets a Float in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setFloat(@NotNull NamespacedKey namespacedKey, @Nullable Float value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Nullable
    @Override
    public Integer getInteger(@NotNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.INTEGER);
    }

    /**
     * Sets an Integer in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setInteger(@NotNull NamespacedKey namespacedKey, @Nullable Integer value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Override
    public @Nullable Boolean getBoolean(@NotNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.BOOLEAN);
    }

    /**
     * Sets a Boolean in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setBoolean(@NotNull NamespacedKey namespacedKey, @Nullable Boolean value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Override
    public void save() {
        obj.setItemMeta(this.meta);
    }

    private @NotNull PersistentDataContainer getData() {
        return this.meta.getPersistentDataContainer();
    }

}
