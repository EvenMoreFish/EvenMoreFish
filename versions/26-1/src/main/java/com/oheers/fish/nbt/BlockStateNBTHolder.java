package com.oheers.fish.nbt;

import com.oheers.fish.items.nbt.NBTHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class BlockStateNBTHolder extends NBTHolder<BlockState> {

    public BlockStateNBTHolder(@NonNull BlockState obj) {
        super(obj);
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
        System.out.println("save not implemented yet.");
    }

    private @Nullable BlockEntity getBlockEntity(@Nullable BlockState state) {
        if (state == null) {
            return null;
        }
        ServerLevel level = ((CraftWorld) state.getWorld()).getHandle();
        return level.getBlockEntity(((CraftBlock) state.getBlock()).getPosition());
    }

    private @Nullable CompoundTag getData(@Nullable String namespace) {
        BlockEntity entity = getBlockEntity(obj);
        if (entity == null) {
            return null;
        }
        CustomData data = entity.components().get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        return namespace == null ? tag : tag.getCompound(namespace).orElse(null);
    }

    private void modifyData(@NotNull Consumer<CompoundTag> consumer) {
        System.out.println("modifyData not implemented yet.");
        return;
        /*
        if (this.data == null) {
            return;
        }
        consumer.accept(this.data);
        if (autoSave) {
            save();
        }
        */
    }

}
