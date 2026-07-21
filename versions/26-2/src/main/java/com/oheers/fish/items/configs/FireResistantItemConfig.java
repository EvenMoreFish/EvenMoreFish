package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DamageResistant;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
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
        return section.getBoolean("fire-resistant", false);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value) {
                DamageResistant damageResistant = DamageResistant.damageResistant(RegistrySet.keySet(DamageTypeTagKeys.IS_FIRE.registryKey()));
                item.setData(DataComponentTypes.DAMAGE_RESISTANT, damageResistant);
            } else {
                item.unsetData(DataComponentTypes.DAMAGE_RESISTANT);
            }
        };
    }

}
