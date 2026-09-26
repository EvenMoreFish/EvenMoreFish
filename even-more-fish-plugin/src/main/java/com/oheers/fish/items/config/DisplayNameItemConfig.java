package com.oheers.fish.items.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.oheers.fish.items.config.ItemConfig;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.replacer.Replacer;
import uk.firedev.daisylib.utils.MessageUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class DisplayNameItemConfig extends ItemConfig<String> {

    public DisplayNameItemConfig(@NonNull Section section) {
        super(section);
    }

    public DisplayNameItemConfig(@NonNull DisplayNameItemConfig base) {
        super(base);
    }

    @Override
    public String getConfiguredValue() {
        return section.getString("displayname");
    }

    @Override
    protected BiConsumer<ItemStack, String> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> {
            if (value == null || value.isEmpty()) {
                item.editMeta(meta -> meta.displayName(Component.empty()));
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
                Component display = ComponentMessage.componentMessage(value)
                    .replace(replacer)
                    .parsePlaceholderAPI(player)
                    .get();
                meta.displayName(display);
            });
        };
    }

    @Override
    public @NonNull DisplayNameItemConfig createCopy() {
        return new DisplayNameItemConfig(this);
    }

}
