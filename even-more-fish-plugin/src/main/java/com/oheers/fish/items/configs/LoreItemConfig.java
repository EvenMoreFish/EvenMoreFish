package com.oheers.fish.items.configs;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.EMFListMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class LoreItemConfig extends ItemConfig<List<Component>> {

    public LoreItemConfig(@NonNull Section section) {
        super(section);
    }

    public LoreItemConfig(@NonNull LoreItemConfig base) {
        super(base);
    }

    @Override
    public List<Component> getConfiguredValue() {
        List<String> lore = section.getStringList("lore");
        return lore.isEmpty() ? null : EMFListMessage.fromStringList(lore).getComponentListMessage();
    }

    @Override
    protected BiConsumer<ItemStack, List<Component>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value.isEmpty()) {
                return;
            }
            item.editMeta(meta -> {
                ComponentListMessage message = ComponentMessage.componentMessage(value)
                    .replace(createReplacer(player, replacements))
                    .parsePlaceholderAPI(player);
                meta.lore(message.get());
            });
        };
    }

    @Override
    public @NonNull LoreItemConfig createCopy() {
        return new LoreItemConfig(this);
    }

    private Replacer createReplacer(OfflinePlayer player, Map<String, ?> replacements) {
        Replacer replacer = Replacer.replacer();
        if (replacements != null && !replacements.isEmpty()) {
            replacer.addReplacements(replacements);
        }
        replacer.addReplacement("{player}", FishUtils.getPlayerNameOrDefault(player, "N/A"));
        return replacer;
    }


}
