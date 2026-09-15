package com.oheers.fish.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

        public void modifyDisplay(@Nullable DisplayNameItemConfig config) {
            Logging.debug("AddonBehavior for the Display Name is set to: " + this);
            if (config == null) {
                return;
            }
            if (!this.equals(REPLACE)) {
                config.setEnabled(false);
            }
        }

        public void modifyLore(@Nullable LoreItemConfig config) {
            Logging.debug("AddonBehavior for the Lore is set to: " + this);
            if (config == null) {
                return;
            }
            switch (this) {
                case NOTHING -> config.setEnabled(false);
                case APPEND -> config.addTransformer((lore, item) -> {
                    List<Component> before = fetchLoreOrEmpty(item);
                    if (lore == null) {
                        return before;
                    }
                    before.addAll(lore);
                    return before;
                });
                case PREPEND -> config.addTransformer((lore, item) -> {
                    List<Component> before = fetchLoreOrEmpty(item);
                    if (lore == null) {
                        return before;
                    }
                    lore.addAll(before);
                    return before;
                });
            }
        }

        private List<Component> fetchLoreOrEmpty(@NonNull ItemStack item) {
            return Optional.ofNullable(item.lore())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        }

    }

}
