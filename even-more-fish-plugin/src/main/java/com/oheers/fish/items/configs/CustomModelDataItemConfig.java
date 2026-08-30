package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class CustomModelDataItemConfig extends ItemConfig<Number> {

    public CustomModelDataItemConfig(@NonNull Section section) {
        super(section);
    }

    public CustomModelDataItemConfig(@NonNull CustomModelDataItemConfig base) {
        super(base);
    }

    @Override
    @NonNull
    public Integer getConfiguredValue() {
        return section.getInt("custom-model-data", null);
    }

    @Override
    protected BiConsumer<ItemStack, Number> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> item.editMeta(meta -> meta.setCustomModelData(value.intValue()));
    }

    @Override
    public @NonNull CustomModelDataItemConfig createCopy() {
        return new CustomModelDataItemConfig(this);
    }

}
