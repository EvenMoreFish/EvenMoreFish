package org.evenmorefish.fish.addons;

import com.oheers.fish.items.configs.ItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class ItemModelItemConfig extends ItemConfig<NamespacedKey> {

    public ItemModelItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public NamespacedKey getConfiguredValue() {
        String keyStr = section.getString("item.item-model");
        if (keyStr == null) {
            return null;
        }
        return NamespacedKey.fromString(keyStr);
    }

    @Override
    protected BiConsumer<ItemStack, NamespacedKey> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return (item, value) -> item.editMeta(
            meta -> meta.setItemModel(value)
        );
    }

}
