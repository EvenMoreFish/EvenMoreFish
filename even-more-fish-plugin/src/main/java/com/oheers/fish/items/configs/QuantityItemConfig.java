package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class QuantityItemConfig extends ItemConfig<Integer> {

    public QuantityItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public @NotNull Integer getConfiguredValue() {
        return section.getInt("item.quantity", 1);
    }

    @Override
    protected BiConsumer<ItemStack, Integer> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return ItemStack::setAmount;
    }

}
