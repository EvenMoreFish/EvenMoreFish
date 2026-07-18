package com.oheers.fish.nbt;

import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackNBTHolder extends NBTHolder<ItemStack> {

    public ItemStackNBTHolder(@NotNull ItemStack obj) {
        super(obj);
    }

    @Override
    public boolean hasNamespace(@NotNull String namespace) {
        return NBT.get(this.obj, nbt -> nbt.getCompound(namespace) != null);
    }

    @Override
    public boolean hasKey(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        return NBT.get(this.obj, nbt -> {
            ReadableNBT compound = nbt.getCompound(namespace);
            return compound != null && compound.hasTag(key);
        });
    }

    @Nullable
    @Override
    public String getString(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        return NBT.get(this.obj, nbt -> {
            ReadableNBT compound = nbt.getCompound(namespace);
            if (compound == null) {
                return null;
            }
            return compound.getString(key);
        });
    }

    /**
     * Sets a String in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setString(@NotNull NamespacedKey namespacedKey, @Nullable String value) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        NBT.modify(this.obj, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(namespace);
            if (value == null) {
                compound.removeKey(key);
            } else {
                compound.setString(key, value);
            }
        });
    }

    @Nullable
    @Override
    public Float getFloat(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        return NBT.get(this.obj, nbt -> {
            ReadableNBT compound = nbt.getCompound(namespace);
            if (compound == null) {
                return null;
            }
            return compound.getFloat(key);
        });
    }

    /**
     * Sets a Float in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setFloat(@NotNull NamespacedKey namespacedKey, @Nullable Float value) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        NBT.modify(this.obj, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(namespace);
            if (value == null) {
                compound.removeKey(key);
            } else {
                compound.setFloat(key, value);
            }
        });
    }

    @Nullable
    @Override
    public Integer getInteger(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        return NBT.get(this.obj, nbt -> {
            ReadableNBT compound = nbt.getCompound(namespace);
            if (compound == null) {
                return null;
            }
            return compound.getInteger(key);
        });
    }

    /**
     * Sets an Integer in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setInteger(@NotNull NamespacedKey namespacedKey, @Nullable Integer value) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        NBT.modify(this.obj, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(namespace);
            if (value == null) {
                compound.removeKey(key);
            } else {
                compound.setInteger(key, value);
            }
        });
    }

    @Nullable
    @Override
    public Boolean getBoolean(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        return NBT.get(this.obj, nbt -> {
            ReadableNBT compound = nbt.getCompound(namespace);
            if (compound == null) {
                return null;
            }
            return compound.getBoolean(key);
        });
    }

    /**
     * Sets a Boolean in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setBoolean(@NotNull NamespacedKey namespacedKey, @Nullable Boolean value) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        NBT.modify(this.obj, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(namespace);
            if (value == null) {
                compound.removeKey(key);
            } else {
                compound.setBoolean(key, value);
            }
        });
    }

    @Override
    public void save() { /* Does nothing because NBT-API saves automatically. */ }

}
