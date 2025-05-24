package com.oheers.fish.items.configs;

import com.oheers.fish.messages.EMFSingleMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class DisplayNameItemConfig extends ItemConfig<String> {

    public DisplayNameItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public String getConfiguredValue() {
        return section.getString("item.displayname");
    }

    @Override
    protected BiConsumer<ItemStack, String> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value == null || value.isEmpty()) {
                item.editMeta(meta -> meta.displayName(Component.empty()));
                return;
            }
            EMFSingleMessage display = EMFSingleMessage.fromString(value);
            display.setVariables(replacements);
            item.editMeta(meta -> meta.displayName(display.getComponentMessage()));
        };
    }

}
