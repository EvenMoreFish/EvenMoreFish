package org.evenmorefish.fish.addons;

import com.oheers.fish.items.configs.ItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class ItemModelItemConfig extends ItemConfig<NamespacedKey> {

    public ItemModelItemConfig(@NonNull Section section) {
        super(section);
    }

    public ItemModelItemConfig(@NonNull ItemModelItemConfig base) {
        super(base);
    }

    @Override
    public NamespacedKey getConfiguredValue() {
        String keyStr = section.getString("item-model");
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

    @Override
    public @NonNull ItemModelItemConfig createCopy() {
        return new ItemModelItemConfig(this);
    }

}
