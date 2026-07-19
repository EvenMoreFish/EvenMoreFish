package com.oheers.fish.nbt;

import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class ItemStackNBTHolder extends NBTHolder<ItemStack> {

    private final @NotNull CompoundTag data;

    public ItemStackNBTHolder(@NotNull ItemStack obj) {
        super(obj);

        net.minecraft.world.item.ItemStack handle = ((CraftItemStack) obj).handle;
        CustomData customData = handle.getComponents().get(DataComponents.CUSTOM_DATA);
        this.data = (customData == null) ? new CompoundTag() : customData.copyTag();
    }

    @Override
    public boolean hasNamespace(@NotNull String namespace) {
        return getData(namespace, false) != null;
    }

    @Override
    public boolean hasKey(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        return data.contains(key);
    }

    @Nullable
    @Override
    public String getString(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (!data.contains(key)) {
            return null;
        }
        return data.getString(key);
    }

    /**
     * Sets a String in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setString(@NotNull NamespacedKey namespacedKey, @Nullable String value) {
        CompoundTag tag = getData(namespacedKey.getNamespace());
        if (value == null) {
            tag.remove(namespacedKey.getKey());
        } else {
            tag.putString(namespacedKey.getKey(), value);
        }
        if (autoSave) {
            save();
        }
    }

    @Nullable
    @Override
    public Float getFloat(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (!data.contains(key)) {
            return null;
        }
        return data.getFloat(key);
    }

    /**
     * Sets a Float in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setFloat(@NotNull NamespacedKey namespacedKey, @Nullable Float value) {
        CompoundTag tag = getData(namespacedKey.getNamespace());
        if (value == null) {
            tag.remove(namespacedKey.getKey());
        } else {
            tag.putFloat(namespacedKey.getKey(), value);
        }
        if (autoSave) {
            save();
        }
    }

    @Nullable
    @Override
    public Integer getInteger(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (!data.contains(key)) {
            return null;
        }
        return data.getInt(key);
    }

    /**
     * Sets an Integer in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setInteger(@NotNull NamespacedKey namespacedKey, @Nullable Integer value) {
        CompoundTag tag = getData(namespacedKey.getNamespace());
        if (value == null) {
            tag.remove(namespacedKey.getKey());
        } else {
            tag.putInt(namespacedKey.getKey(), value);
        }
        if (autoSave) {
            save();
        }
    }

    @Nullable
    @Override
    public Boolean getBoolean(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (!data.contains(key)) {
            return null;
        }
        return data.getBoolean(key);
    }

    /**
     * Sets a Boolean in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setBoolean(@NotNull NamespacedKey namespacedKey, @Nullable Boolean value) {
        CompoundTag tag = getData(namespacedKey.getNamespace());
        if (value == null) {
            tag.remove(namespacedKey.getKey());
        } else {
            tag.putBoolean(namespacedKey.getKey(), value);
        }
        if (autoSave) {
            save();
        }
    }

    @Override
    public void save() {
        ((CraftItemStack) obj).handle.set(DataComponents.CUSTOM_DATA, CustomData.of(this.data));
    }

    private @NotNull CompoundTag getData() {
        return this.data;
    }

    private @NotNull CompoundTag getData(@NotNull String namespace) {
        CompoundTag tag = getData(namespace, true);
        if (tag == null) {
            throw new IllegalStateException("Null tag was incorrectly returned.");
        }
        return tag;
    }

    private @Nullable CompoundTag getData(@NotNull String namespace, boolean create) {
        if (this.data.contains(namespace)) {
            return this.data.getCompound(namespace);
        }
        if (create) {
            CompoundTag created = new CompoundTag();
            this.data.put(namespace, created);
            return created;
        } else {
            return null;
        }
    }

}
