package com.oheers.fish.items.configs;

import com.oheers.fish.utils.ItemUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class GlowingItemConfig extends ItemConfig<@NotNull Boolean> {

    public GlowingItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public @NotNull Boolean getConfiguredValue() {
        return section.getBoolean("item.glowing", false);
    }

    @Override
    protected BiConsumer<ItemStack, @NotNull Boolean> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value) {
                ItemUtils.glowify(item);
            }
        };
    }

}
