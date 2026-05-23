package com.oheers.fish.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.configs.ItemConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        public void applyDisplay(@NotNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NotNull ItemConfig<String> display) {
            System.out.println("Addon Display configured to " + this);
            if (this.equals(REPLACE)) {
                display.apply(item, player, replacements);
            }
        }

        // Could be slightly confusing. May need to be rewritten.
        public void applyLore(@NotNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NotNull ItemConfig<List<String>> lore) {
            System.out.println("Addon Lore configured to " + this);
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

        private List<Component> fetchLoreOrEmpty(@NotNull ItemStack item) {
            return Optional.ofNullable(item.lore())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        }

    }

}
