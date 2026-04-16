package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class EmptyItemConfig<T> extends ItemConfig<T> {

    public EmptyItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public T getConfiguredValue() {
        return null;
    }

    @Override
    protected BiConsumer<ItemStack, T> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            // Do nothing
        };
    }

}
