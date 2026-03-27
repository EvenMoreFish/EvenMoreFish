package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class GlowingItemConfig extends ItemConfig<Boolean> {

    public GlowingItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public @NotNull Boolean getConfiguredValue() {
        return section.getBoolean("item.glowing", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value) {
                item.editMeta(meta -> {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                });
            }
        };
    }

}
