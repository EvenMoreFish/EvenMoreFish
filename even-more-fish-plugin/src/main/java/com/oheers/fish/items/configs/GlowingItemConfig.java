package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class GlowingItemConfig extends ItemConfig<Boolean> {

    public GlowingItemConfig(@NonNull Section section) {
        super(section);
    }

    @Override
    public @NonNull Boolean getConfiguredValue() {
        return section.getBoolean("glowing", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
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
