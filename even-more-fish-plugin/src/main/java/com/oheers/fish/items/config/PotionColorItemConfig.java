package com.oheers.fish.items.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class PotionColorItemConfig extends ItemConfig<Color> {

    public PotionColorItemConfig(@NonNull Section section) {
        super(section);
    }

    public PotionColorItemConfig(@NonNull PotionColorItemConfig base) {
        super(base);
    }

    @Override
    public Color getConfiguredValue() {
        String colorString = section.getString("potion-colour", section.getString("potion-color"));
        if (colorString == null) {
            return null;
        }
        try {
            java.awt.Color color = java.awt.Color.decode(colorString);
            return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    protected BiConsumer<ItemStack, Color> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value == null) {
                return;
            }
            item.editMeta(PotionMeta.class, meta -> meta.setColor(value));
        };
    }

    @Override
    public @NonNull PotionColorItemConfig createCopy() {
        return new PotionColorItemConfig(this);
    }

}
