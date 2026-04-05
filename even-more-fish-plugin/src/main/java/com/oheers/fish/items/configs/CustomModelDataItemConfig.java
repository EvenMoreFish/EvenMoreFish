package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class CustomModelDataItemConfig extends ItemConfig<Number> {

    public CustomModelDataItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    @NotNull
    public Integer getConfiguredValue() {
        return section.getInt("item.custom-model-data", null);
    }

    @Override
    protected BiConsumer<ItemStack, Number> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> item.editMeta(meta -> meta.setCustomModelData(value.intValue()));
    }

}
