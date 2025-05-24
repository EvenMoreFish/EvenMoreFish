package com.oheers.fish.items;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

public class ItemFactoryConversion {

    private boolean changed = false;

    public void performConversions(@NotNull Section section) {
        moveIfPresent(section, "glowing", "item.glowing");
        moveIfPresent(section, "dye-colour", "item.dye-colour");
        moveIfPresent(section, "durability", "item.durability");
        moveIfPresent(section, "lore", "item.lore");
        moveIfPresent(section, "displayname", "item.displayname");

        if (changed) {
            try {
                section.getRoot().save();
            } catch (Exception e) {
                throw new RuntimeException("Failed to save item config", e);
            }
        }
    }

    private void moveIfPresent(@NotNull Section section, @NotNull String from, @NotNull String to) {
        if (section.contains(from) && !section.contains(to)) {
            section.set(to, section.get(from));
            section.remove(from);
            changed = true;
        }
    }

}
