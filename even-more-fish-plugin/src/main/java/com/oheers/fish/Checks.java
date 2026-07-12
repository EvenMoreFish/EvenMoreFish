package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.exploits.AFKFishingTracker;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A utility class for common checks
 */
@ApiStatus.Internal
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
    public static boolean isMcMMOOverfishing(@NotNull Player player, @NotNull Location hookLocation) {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingMcMMO()) {
            return false;
        }
        if (!ExperienceConfig.getInstance().isFishingExploitingPrevented()) {
            return false;
        }
        McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        // The deprecated method has to be used here for older mcmmo versions.
        //noinspection removal
        return mmoPlayer != null && mmoPlayer.getFishingManager().isExploitingFishing(hookLocation.toVector());
    }

    public static boolean canFishInWorld(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        List<String> whitelistedWorlds = MainConfig.getInstance().getAllowedWorlds();
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world.getName()) || whitelistedWorlds.contains(world.getKey().asString());
    }

    public static boolean canFishInRegion(@NotNull Location location) {
        return canUseRegion(location, MainConfig.getInstance().getAllowedRegions());
    }

    public static boolean canUseRegion(@NotNull Location location, @NotNull List<String> allowedRegions) {
        // If no whitelist is defined, allow all regions
        if (allowedRegions.isEmpty()) {
            return true;
        }

        boolean worldGuard = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
        boolean redProtect = Bukkit.getPluginManager().isPluginEnabled("RedProtect");

        if (!worldGuard && !redProtect) {
            EvenMoreFish.getInstance().getLogger().warning("Please install WorldGuard or RedProtect to check regions.");
            return true;
        }

        // No region found in RedProtect
        if (worldGuard) {
            // Check WorldGuard
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            Set<ProtectedRegion> regions = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
            return regions.stream().anyMatch(region -> allowedRegions.contains(region.getId()));
        } else {
            // Check RedProtect
            Region region = RedProtect.get().getAPI().getRegion(location);
            return region != null && allowedRegions.contains(region.getName());
        }
    }

    public static boolean isAFKFishing(@NotNull Player player) {
        if (!MainConfig.getInstance().isAFKProtectionEnabled()) {
            return false;
        }
        return AFKFishingTracker.get(player.getUniqueId()).isAFKFishing();
    }

}
