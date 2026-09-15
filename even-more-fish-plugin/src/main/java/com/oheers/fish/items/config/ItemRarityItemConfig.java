package com.oheers.fish.items.config;

import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.oheers.fish.items.config.ItemConfig;
import uk.firedev.daisylib.utils.CommonUtils;

import java.util.Map;
import java.util.function.BiConsumer;

public class ItemRarityItemConfig extends ItemConfig<String> {

    public ItemRarityItemConfig(@NonNull Section section) {
        super(section);
    }

    public ItemRarityItemConfig(@NonNull ItemRarityItemConfig base) {
        super(base);
    }

    @Override
    public String getConfiguredValue() {
        return section.getString("item-rarity");
    }

    @Override
    protected BiConsumer<ItemStack, String> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            ItemRarity rarity = CommonUtils.getEnumValue(ItemRarity.class, value);
            if (rarity != null) {
                item.editMeta(meta -> meta.setRarity(rarity));
            }
        };
    }

    @Override
    public @NonNull ItemRarityItemConfig createCopy() {
        return new ItemRarityItemConfig(this);
    }

}
