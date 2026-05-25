package com.oheers.fish.items.configs;

import com.oheers.fish.messages.EMFListMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class LoreItemConfig extends ItemConfig<List<Component>> {

    public LoreItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public List<Component> getConfiguredValue() {
        List<String> lore = section.getStringList("item.lore");
        return lore.isEmpty() ? null : EMFListMessage.fromStringList(lore).getComponentListMessage();
    }

    @Override
    protected BiConsumer<ItemStack, List<Component>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value.isEmpty()) {
                return;
            }
            EMFListMessage lore = EMFListMessage.ofList(value);
            lore.setVariables(replacements);
            item.editMeta(meta -> meta.lore(lore.getComponentListMessage(player)));
        };
    }


}
