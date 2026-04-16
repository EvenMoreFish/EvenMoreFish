package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class FireResistantItemConfig extends ItemConfig<Boolean> {

    public FireResistantItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("item.fire-resistant", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) ->
            item.editMeta(meta -> meta.setFireResistant(value));
    }

}
