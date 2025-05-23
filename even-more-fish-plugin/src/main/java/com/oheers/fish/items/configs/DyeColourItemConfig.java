package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Color;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class DyeColourItemConfig extends ItemConfig<Color> {

    public DyeColourItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Color getConfiguredValue() {
        String colourString = section.getString("item.dye-colour");
        if (colourString == null) {
            return null;
        }
        try {
            java.awt.Color color = java.awt.Color.decode(colourString);
            return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    protected BiConsumer<ItemStack, Color> applyToItem(@Nullable Map<String, ?> replacements) {
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

}
