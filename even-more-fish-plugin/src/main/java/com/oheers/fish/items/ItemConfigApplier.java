package com.oheers.fish.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.ItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles custom ItemConfig behavior for item addons.
 */
@ApiStatus.Internal
public class ItemConfigApplier {

    public static void apply(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull ItemConfig<?> config, boolean usingAddon) {
        if (!usingAddon) { // If not using an addon, we can apply normally.
            config.apply(item, player, replacements);
            return;
        }

        if (config instanceof DisplayNameItemConfig display) {
            AddonBehavior behavior = AddonBehavior.getOrDefault(MainConfig.getInstance().getDisplayNameAddonBehavior(), AddonBehavior.REPLACE);
            behavior.applyDisplay(item, player, replacements, display);
            return;
        }
        if (config instanceof LoreItemConfig lore) {
            AddonBehavior behavior = AddonBehavior.getOrDefault(MainConfig.getInstance().getLoreAddonBehavior(), AddonBehavior.REPLACE);
            behavior.applyLore(item, player, replacements, lore);
            return;
        }
        config.apply(item, player, replacements);
    }

    public enum AddonBehavior {
        NOTHING,
        APPEND,
        PREPEND,
        REPLACE;

        public static @NonNull AddonBehavior getOrDefault(@Nullable String behavior, @NonNull AddonBehavior def) {
            return FishUtils.getEnumValue(AddonBehavior.class, behavior, def);
        }

        public void applyDisplay(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull DisplayNameItemConfig display) {
            Logging.debug("AddonBehavior for the Display Name is set to: " + this);
            if (this.equals(REPLACE)) {
                display.apply(item, player, replacements);
            }
        }

        public void applyLore(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull LoreItemConfig lore) {
            Logging.debug("AddonBehavior for the Lore is set to: " + this);
            switch (this) {
                case REPLACE -> lore.apply(item, player, replacements);
                case NOTHING -> {}
                case APPEND -> {
                    List<Component> before = fetchLoreOrEmpty(item);
                    System.out.println(before);
                    lore.apply(item, player, replacements);
                    List<Component> after = item.lore();
                    System.out.println(after);
                    if (after != null) {
                        before.addAll(after);
                        System.out.println(before);
                        item.editMeta(meta -> meta.lore(before));
                    }
                }
                case PREPEND -> {
                    List<Component> before = item.lore();
                    lore.apply(item, player, replacements);
                    List<Component> after = fetchLoreOrEmpty(item);
                    if (before != null) {
                        after.addAll(before);
                        item.editMeta(meta -> meta.lore(after));
                    }
                }
            }
        }

        private List<Component> fetchLoreOrEmpty(@NonNull ItemStack item) {
            return Optional.ofNullable(item.lore())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        }

    }

}