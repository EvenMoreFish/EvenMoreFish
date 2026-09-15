package com.oheers.fish.items.config;

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

public class TooltipStyleItemConfig extends ItemConfig<NamespacedKey> {

    private static final Method SETTER = ReflectionUtils.getMethodOrNull(ItemMeta.class, "setTooltipStyle", NamespacedKey.class);

    public TooltipStyleItemConfig(@NonNull Section section) {
        super(section);
    }

    public TooltipStyleItemConfig(@NonNull TooltipStyleItemConfig base) {
        super(base);
    }

    @Override
    public @Nullable NamespacedKey getConfiguredValue() {
        String keyStr = section.getString("tooltip-style");
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
                    DaisyLib.get().getLogging().warn("Failed to apply tooltip-style config", exception);
                }
            });
        };
    }

    @Override
    public @NonNull TooltipStyleItemConfig createCopy() {
        return new TooltipStyleItemConfig(this);
    }

}
