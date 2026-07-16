package com.oheers.fish.items.nbt;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SkullNBTHolder extends NBTHolder<Skull> {

    public SkullNBTHolder(@NonNull Skull obj) {
        super(obj);
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
        if (value == null) {
            getData().remove(namespacedKey);
            return;
        }
        getData().set(namespacedKey, PersistentDataType.STRING, value);
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
        if (value == null) {
            getData().remove(namespacedKey);
            return;
        }
        getData().set(namespacedKey, PersistentDataType.FLOAT, value);
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
        if (value == null) {
            getData().remove(namespacedKey);
            return;
        }
        getData().set(namespacedKey, PersistentDataType.INTEGER, value);
    }

    @Override
    public void save() { /* PDC saves after being edited, so this does nothing. */ }

    private @NotNull PersistentDataContainer getData() {
        return obj.getPersistentDataContainer();
    }

}
