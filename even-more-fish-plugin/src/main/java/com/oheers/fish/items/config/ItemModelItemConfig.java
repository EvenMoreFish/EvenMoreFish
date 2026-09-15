package com.oheers.fish.items.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.DaisyLib;
import com.oheers.fish.items.config.ItemConfig;
import uk.firedev.daisylib.utils.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;

public class ItemModelItemConfig extends ItemConfig<NamespacedKey> {

    private static final Method SETTER = ReflectionUtils.getMethodOrNull(ItemMeta.class, "setItemModel", NamespacedKey.class);

    public ItemModelItemConfig(@NonNull Section section) {
        super(section);
    }

    public ItemModelItemConfig(@NonNull ItemModelItemConfig base) {
        super(base);
    }

    @Override
    public @Nullable NamespacedKey getConfiguredValue() {
        String keyStr = section.getString("item-model");
        if (keyStr == null) {
            return null;
        }
        return NamespacedKey.fromString(keyStr);
    }

    @Override
    protected BiConsumer<ItemStack, NamespacedKey> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (SETTER == null) {
                return;
            }
            item.editMeta(meta -> {
                try {
                    SETTER.invoke(meta, value);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    DaisyLib.get().getLogging().warn("Failed to apply item-model config", exception);
                }
            });
        };
    }

    @Override
    public @NonNull ItemModelItemConfig createCopy() {
        return new ItemModelItemConfig(this);
    }

}
