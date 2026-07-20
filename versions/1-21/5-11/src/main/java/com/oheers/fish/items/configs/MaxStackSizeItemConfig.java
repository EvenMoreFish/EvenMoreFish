package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class MaxStackSizeItemConfig extends ItemConfig<Integer> {

    public MaxStackSizeItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public @Nullable Integer getConfiguredValue() {
        return section.getInt("item.max-stack-size", null);
    }

    @Override
    protected BiConsumer<ItemStack, Integer> applyToItem(@org.jetbrains.annotations.Nullable OfflinePlayer player, @org.jetbrains.annotations.Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value == null) {
                return;
            }
            int finalValue = Math.clamp(value, 1, 99);
            item.editMeta(meta -> meta.setMaxStackSize(finalValue));
        };
    }

}
