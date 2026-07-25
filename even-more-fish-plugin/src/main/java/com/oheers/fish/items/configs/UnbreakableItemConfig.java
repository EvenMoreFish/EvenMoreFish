package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class UnbreakableItemConfig extends ItemConfig<Boolean> {

    public UnbreakableItemConfig(@NonNull Section section) {
        super(section);
    }

    @Override
    public @NonNull Boolean getConfiguredValue() {
        return section.getBoolean("unbreakable", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> item.editMeta(meta -> meta.setUnbreakable(value));
    }

}
