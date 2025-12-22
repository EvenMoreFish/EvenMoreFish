package com.oheers.fish.items.configs;

import com.oheers.fish.FishUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class ItemRarityItemConfig extends ItemConfig<String> {

    public ItemRarityItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public String getConfiguredValue() {
        return section.getString("item.item-rarity");
    }

    @Override
    protected BiConsumer<ItemStack, String> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            ItemRarity rarity = FishUtils.getEnumValue(ItemRarity.class, value);
            if (rarity != null) {
                item.editMeta(meta -> meta.setRarity(rarity));
            }
        };
    }

}
