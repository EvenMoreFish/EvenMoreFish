package com.oheers.fish.items.configs;

import com.oheers.fish.items.configs.ItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class HideTooltipItemConfig extends ItemConfig<Boolean> {

    public HideTooltipItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("item.hide-tooltip", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) ->
            item.editMeta(meta -> meta.setHideTooltip(value));
    }

}
