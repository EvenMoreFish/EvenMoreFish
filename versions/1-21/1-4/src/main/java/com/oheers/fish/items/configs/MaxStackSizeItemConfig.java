package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class MaxStackSizeItemConfig extends ItemConfig<Integer> {

    public MaxStackSizeItemConfig(@NonNull Section section) {
        super(section);
    }

    @Override
    public @Nullable Integer getConfiguredValue() {
        return section.getInt("max-stack-size", null);
    }

    @Override
    protected BiConsumer<ItemStack, Integer> applyToItem(@org.jspecify.annotations.Nullable OfflinePlayer player, @org.jspecify.annotations.Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value == null) {
                return;
            }
            int finalValue = Math.clamp(value, 1, 99);
            item.editMeta(meta -> meta.setMaxStackSize(finalValue));
        };
    }

}
