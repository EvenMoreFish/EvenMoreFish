package com.oheers.fish.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.configs.ItemConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemFactoryConfig {

    public static AddonBehavior getAddonLoreBehavior() {
        return FishUtils.getEnumValue(
            AddonBehavior.class,
            MainConfig.getInstance().getConfig().getString("items.addons.lore-behavior"),
            AddonBehavior.NOTHING
        );
    }

    public static AddonBehavior getAddonDisplayBehavior() {
        return FishUtils.getEnumValue(
            AddonBehavior.class,
            MainConfig.getInstance().getConfig().getString("items.addons.display-behavior"),
            AddonBehavior.NOTHING
        );
    }

    public enum AddonBehavior {
        NOTHING,
        APPEND,
        PREPEND,
        REPLACE;

        public void applyDisplay(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull ItemConfig<String> display) {
            Logging.debug("AddonBehavior for the Display Name is set to: " + this);
            if (this.equals(REPLACE)) {
                display.apply(item, player, replacements);
            }
        }

        // Could be slightly confusing. May need to be rewritten.
        public void applyLore(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull ItemConfig<List<Component>> lore) {
            Logging.debug("AddonBehavior for the Lore is set to: " + this);
            switch (this) {
                case REPLACE -> lore.apply(item, player, replacements);
                case NOTHING -> {}
                case APPEND -> {
                    List<Component> before = fetchLoreOrEmpty(item);
                    lore.apply(item, player, replacements);
                    List<Component> after = item.lore();
                    if (after != null) {
                        before.addAll(after);
                        item.lore(before);
                    }
                }
                case PREPEND -> {
                    List<Component> before = item.lore();
                    lore.apply(item, player, replacements);
                    List<Component> after = fetchLoreOrEmpty(item);
                    if (before != null) {
                        after.addAll(before);
                        item.lore(after);
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
