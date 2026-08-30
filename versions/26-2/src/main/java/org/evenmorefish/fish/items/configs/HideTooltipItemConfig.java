package org.evenmorefish.fish.items.configs;

import com.oheers.fish.items.configs.ItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class HideTooltipItemConfig extends ItemConfig<Boolean> {

    public HideTooltipItemConfig(@NonNull Section section) {
        super(section);
    }

    public HideTooltipItemConfig(@NonNull HideTooltipItemConfig base) {
        super(base);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("hide-tooltip", false);
    }

    @Override
    protected BiConsumer<ItemStack, Boolean> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) ->
            item.editMeta(meta -> meta.setHideTooltip(value));
    }

    @Override
    public @NonNull HideTooltipItemConfig createCopy() {
        return new HideTooltipItemConfig(this);
    }

}
