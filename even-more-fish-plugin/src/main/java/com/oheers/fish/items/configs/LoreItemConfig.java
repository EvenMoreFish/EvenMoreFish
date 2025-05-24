package com.oheers.fish.items.configs;

import com.oheers.fish.messages.EMFListMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class LoreItemConfig extends ItemConfig<List<String>> {

    public LoreItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public List<String> getConfiguredValue() {
        return section.getStringList("item.lore");
    }

    @Override
    protected BiConsumer<ItemStack, List<String>> applyToItem(@Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value.isEmpty()) {
                return;
            }
            EMFListMessage lore = EMFListMessage.fromStringList(value);
            lore.setVariables(replacements);
            item.editMeta(meta -> meta.lore(lore.getComponentListMessage()));
        };
    }

}
