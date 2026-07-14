package com.oheers.fish.nbt;

import com.oheers.fish.items.nbt.NBTHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ItemStackNBTHolder extends NBTHolder<ItemStack> {

    private @Nullable CompoundTag data;

    public ItemStackNBTHolder(@NonNull ItemStack obj) {
        super(obj);

        net.minecraft.world.item.ItemStack handle = ((CraftItemStack) obj).handle;
        CustomData customData = handle.getComponents().get(DataComponents.CUSTOM_DATA);
        this.data = (customData == null) ? new CompoundTag() : customData.copyTag();
    }

    @Override
    public boolean hasKey(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (data == null) {
            return false;
        }
        return data.contains(key);
    }

    @Nullable
    @Override
    public String getString(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (data == null) {
            return null;
        }
        return data.getString(key).orElse(null);
    }

    /**
     * Sets a String in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setString(@NotNull NamespacedKey namespacedKey, @Nullable String value) {
        modifyData(data -> {
            String namespace = namespacedKey.getNamespace();
            String key = namespacedKey.getKey();
            CompoundTag tag = data.getCompoundOrEmpty(namespace);

            if (value == null) {
                tag.remove(key);
            } else {
                tag.putString(key, value);
            }
        });
    }

    @Nullable
    @Override
    public Float getFloat(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (data == null) {
            return null;
        }
        return data.getFloat(key).orElse(null);
    }

    /**
     * Sets a Float in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setFloat(@NotNull NamespacedKey namespacedKey, @Nullable Float value) {
        modifyData(data -> {
            String namespace = namespacedKey.getNamespace();
            String key = namespacedKey.getKey();
            CompoundTag tag = data.getCompoundOrEmpty(namespace);

            if (value == null) {
                tag.remove(key);
            } else {
                tag.putFloat(key, value);
            }
        });
    }

    @Nullable
    @Override
    public Integer getInteger(@NotNull NamespacedKey namespacedKey) {
        String namespace = namespacedKey.getNamespace();
        String key = namespacedKey.getKey();
        CompoundTag data = getData(namespace);
        if (data == null) {
            return null;
        }
        return data.getInt(key).orElse(null);
    }

    /**
     * Sets an Integer in the specified NBT location. If null is passed, the key will be removed.
     */
    @Override
    public void setInteger(@NotNull NamespacedKey namespacedKey, @Nullable Integer value) {
        modifyData(data -> {
            String namespace = namespacedKey.getNamespace();
            String key = namespacedKey.getKey();
            CompoundTag tag = data.getCompoundOrEmpty(namespace);

            if (value == null) {
                tag.remove(key);
            } else {
                tag.putInt(key, value);
            }
        });
    }

    @Override
    public void save() {
        if (this.data == null) {
            return;
        }
        ((CraftItemStack) obj).handle.set(DataComponents.CUSTOM_DATA, CustomData.of(this.data));
    }

    private void modifyData(@NotNull Consumer<CompoundTag> consumer) {
        if (this.data == null) {
            return;
        }
        consumer.accept(this.data);
        if (autoSave) {
            save();
        }
    }

}
