package com.oheers.fish.items;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

public class ItemFactoryConversion {

    public void performConversions(@NotNull Section section) {
        copyIfPresent(section, "glowing", "item.glowing");
        copyIfPresent(section, "dye-colour", "item.dye-colour");
        copyIfPresent(section, "durability", "item.durability");
        copyIfPresent(section, "lore", "item.lore");
        copyIfPresent(section, "displayname", "item.displayname");
    }

    private void copyIfPresent(@NotNull Section section, @NotNull String from, @NotNull String to) {
        if (section.contains(from) && !section.contains(to)) {
            section.set(to, section.get(from));
        }
    }

}
