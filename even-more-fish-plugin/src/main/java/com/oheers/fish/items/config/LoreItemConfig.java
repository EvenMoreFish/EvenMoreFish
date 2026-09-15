package com.oheers.fish.items.config;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.oheers.fish.items.config.ItemConfig;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.replacer.Replacer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return lore.isEmpty() ? null : ComponentMessage.componentMessage(lore).get();
    }

    @Override
    protected BiConsumer<ItemStack, List<Component>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value.isEmpty()) {
                return;
            }
            String name = Optional.ofNullable(player)
                .map(OfflinePlayer::getName)
                .orElse("N/A");
            Replacer replacer = Replacer.replacer().addReplacement("{player}", name);
            if (replacements != null) {
                replacer.addReplacements(replacements);
            }
            item.editMeta(meta -> {
                List<Component> lore = ComponentMessage.componentMessage(value)
                    .replace(replacer)
                    .parsePlaceholderAPI(player)
                    .get();
                meta.lore(lore);
            });
        };
    }

    @Override
    public @NonNull LoreItemConfig createCopy() {
        return new LoreItemConfig(this);
    }

}
