package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CustomModelDataItemConfig extends ItemConfig<Number> {

    public CustomModelDataItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    @NotNull
    public Float getConfiguredValue() {
        return section.getFloat("custom-model-data", null);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected BiConsumer<ItemStack, Number> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> item.editMeta(meta -> {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            component.setFloats(List.of(value.floatValue()));
            meta.setCustomModelDataComponent(component);
        });
    }

}
