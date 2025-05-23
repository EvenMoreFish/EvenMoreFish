package com.oheers.fish.items.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class PotionMetaItemConfig extends ItemConfig<PotionEffect> {

    public PotionMetaItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public PotionEffect getConfiguredValue() {
        String potionSettings = section.getString("item.potion");
        if (potionSettings == null) {
            return null;
        }
        return FishUtils.getPotionEffect(potionSettings);
    }

    @Override
    public BiConsumer<ItemStack, PotionEffect> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value == null) {
                EvenMoreFish.getInstance().getLogger().severe(section.getRouteAsString() + " has invalid potion effect");
                return;
            }
            item.editMeta(PotionMeta.class, meta -> meta.addCustomEffect(value, true));
        };
    }

}
