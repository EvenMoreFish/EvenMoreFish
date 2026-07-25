package com.oheers.fish.items.nbt;

import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class LegacyItemStackNBTHolder extends NBTHolder<ItemStack> {

    private final ItemMeta meta;

    public LegacyItemStackNBTHolder(@NonNull ItemStack obj) {
        super(obj);
        this.meta = obj.getItemMeta();
    }

    @Override
    public boolean hasNamespace(@NonNull String namespace) {
        return getData().getKeys().stream()
            .anyMatch(key -> key.getNamespace().equals(namespace.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean hasKey(@NonNull NamespacedKey namespacedKey) {
        return getData().has(namespacedKey);
    }

    @Nullable
    @Override
    public String getString(@NonNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * Sets a String in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setString(@NonNull NamespacedKey namespacedKey, @Nullable String value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Nullable
    @Override
    public Float getFloat(@NonNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.FLOAT);
    }

    /**
     * Sets a Float in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setFloat(@NonNull NamespacedKey namespacedKey, @Nullable Float value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Nullable
    @Override
    public Integer getInteger(@NonNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.INTEGER);
    }

    /**
     * Sets an Integer in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setInteger(@NonNull NamespacedKey namespacedKey, @Nullable Integer value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Override
    public @Nullable Boolean getBoolean(@NonNull NamespacedKey namespacedKey) {
        return getData().get(namespacedKey, PersistentDataType.BOOLEAN);
    }

    /**
     * Sets a Boolean in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setBoolean(@NonNull NamespacedKey namespacedKey, @Nullable Boolean value) {
        throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
    }

    @Override
    public void save() {
        obj.setItemMeta(this.meta);
    }

    private @NonNull PersistentDataContainer getData() {
        return this.meta.getPersistentDataContainer();
    }

}
