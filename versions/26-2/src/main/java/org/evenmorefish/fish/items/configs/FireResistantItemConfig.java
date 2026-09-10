package org.evenmorefish.fish.items.configs;

import com.oheers.fish.items.configs.ItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DamageResistant;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("UnstableApiUsage")
public class FireResistantItemConfig extends ItemConfig<Boolean> {

    public FireResistantItemConfig(@NonNull Section section) {
        super(section);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("fire-resistant", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value) {
                item.setData(
                    DataComponentTypes.DAMAGE_RESISTANT,
                    DamageResistant.damageResistant(RegistrySet.keySet(DamageTypeTagKeys.IS_FIRE.registryKey()))
                );
            } else {
                item.unsetData(DataComponentTypes.DAMAGE_RESISTANT);
            }
        };
    }

}
