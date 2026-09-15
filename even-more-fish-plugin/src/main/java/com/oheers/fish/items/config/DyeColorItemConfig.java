package com.oheers.fish.items.config;

import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.oheers.fish.items.config.ItemConfig;

import java.util.Map;
import java.util.function.BiConsumer;

public class DyeColorItemConfig extends ItemConfig<Color> {

    public DyeColorItemConfig(@NonNull Section section) {
        super(section);
    }

    public DyeColorItemConfig(@NonNull DyeColorItemConfig base) {
        super(base);
    }

    @Override
    public Color getConfiguredValue() {
        String colorString = section.getString("dye-colour", section.getString("dye-color"));
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
            item.editMeta(LeatherArmorMeta.class, meta -> {
                meta.setColor(value);
                meta.addItemFlags(ItemFlag.HIDE_DYE);
            });
        };
    }

    @Override
    public @NonNull DyeColorItemConfig createCopy() {
        return new DyeColorItemConfig(this);
    }

}
