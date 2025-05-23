package com.oheers.fish.items.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

public class ItemDamageItemConfig extends ItemConfig<@NotNull Integer> {

    public ItemDamageItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public @NotNull Integer getConfiguredValue() {
        return section.getInt("item.durability");
    }

    @Override
    protected BiConsumer<ItemStack, @NotNull Integer> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            int maxDurability = item.getType().getMaxDurability();
            item.editMeta(Damageable.class, meta -> {
                if (value >= 0 && value <= 100) {
                    int finalDurability = value / 100 * maxDurability;
                    meta.setDamage(finalDurability);
                } else if (MainConfig.getInstance().doingRandomDurability()) {
                    Random random = EvenMoreFish.getInstance().getRandom();
                    meta.setDamage(random.nextInt() * (maxDurability + 1));
                }
            });
        };
    }

}
