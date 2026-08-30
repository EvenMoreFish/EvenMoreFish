package com.oheers.fish.items.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

public class ItemDamageItemConfig extends ItemConfig<Integer> {

    public ItemDamageItemConfig(@NonNull Section section) {
        super(section);
    }

    public ItemDamageItemConfig(@NonNull ItemDamageItemConfig base) {
        super(base);
    }

    @Override
    public @NonNull Integer getConfiguredValue() {
        return section.getInt("durability");
    }

    @Override
    protected BiConsumer<ItemStack, Integer> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            int maxDurability = item.getType().getMaxDurability();
            item.editMeta(Damageable.class, meta -> {
                if (value >= 0 && value <= 100) {
                    int finalDurability = value / 100 * maxDurability;
                    meta.setDamage(finalDurability);
                } else if (MainConfig.getInstance().doingRandomDurability()) {
                    Random random = EvenMoreFish.RANDOM;
                    meta.setDamage(random.nextInt() * (maxDurability + 1));
                }
            });
        };
    }

    @Override
    public @NonNull ItemDamageItemConfig createCopy() {
        return new ItemDamageItemConfig(this);
    }

}
