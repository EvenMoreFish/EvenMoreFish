package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class ModernGlowingItemConfig extends ItemConfig<Boolean> {

    public ModernGlowingItemConfig(@NonNull Section section) {
        super(section);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("glowing");
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) ->
            item.editMeta(meta -> meta.setEnchantmentGlintOverride(value));
    }

}
