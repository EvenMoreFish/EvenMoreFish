package com.oheers.fish;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for common checks
 */
public class Checks {

    /**
     * Checks if the player can use a fishing rod.
     * @param item The fishing rod to check.
     */
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

    /**
     * Checks if the player is overfishing if mcMMO is installed.
     * @param player The player to check.
     * @param location The location of the hook.
     */
    public static boolean isMcMMOOverfishing(@NotNull Player player, @NotNull Location location) {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingMcMMO()) {
            return false;
        }
        if (!ExperienceConfig.getInstance().isFishingExploitingPrevented()) {
            return false;
        }
        McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        return mmoPlayer != null && mmoPlayer.getFishingManager().isExploitingFishing(location.toVector());
    }

}
