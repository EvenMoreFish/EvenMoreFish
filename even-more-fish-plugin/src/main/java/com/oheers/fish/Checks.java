package com.oheers.fish;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for common checks
 */
public class Checks {

    public static boolean canUseRod(@Nullable ItemStack item) {
        if (item == null || !item.getType().equals(Material.FISHING_ROD)) {
            return false;
        }
        if (!MainConfig.getInstance().requireCustomRod()) {
            return true;
        }
        CustomRod customRod = RodManager.getInstance().getRod(item);
        return customRod != null;
    }

}
