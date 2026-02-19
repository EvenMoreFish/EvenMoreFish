package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class CustomModelDataItemConfig extends ItemConfig<Float> {

    public CustomModelDataItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    @NotNull
    public Float getConfiguredValue() {
        return section.getFloat("item.custom-model-data");
    }

    @Override
    protected BiConsumer<ItemStack, Float> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value != 0) {
                item.editMeta(meta -> meta.setCustomModelData(Math.round(value)));
            }
        };
    }

}
