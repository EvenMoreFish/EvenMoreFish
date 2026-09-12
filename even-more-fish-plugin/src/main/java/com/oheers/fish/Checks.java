package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.skills.fishing.FishingManager;
import com.gmail.nossr50.util.player.UserManager;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.exploits.AFKFishingTracker;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * A utility class for common checks
 */
@ApiStatus.Internal
public class Checks {

    // The deprecated isExploitingFishing(Vector) method. If the replacement is present, this is null.
    private static final Method DEPRECATED_IS_EXPLOITING_FISHING = initDeprecatedMcMMOMethod();

    private static @Nullable Method initDeprecatedMcMMOMethod() {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingMcMMO()) {
            return null;
        }
        Method method = FishUtils.getMethodOrNull(FishingManager.class, "isExploitingFishing");
        if (method != null) {
            Logging.debug("Modern mcMMO isExploitingFishing method found.");
            return null;
        }
        Method deprecatedMethod = FishUtils.getMethodOrNull(FishingManager.class, "isExploitingFishing", Vector.class);
        if (deprecatedMethod == null) {
            Logging.warn("Could not find mcMMO isExploitingFishing method. Overfishing checks will not work.");
        }
        Logging.debug("Deprecated mcMMO isExploitingFishing method found.");
        return deprecatedMethod;
    }

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
    public static boolean isMcMMOOverfishing(@NonNull Player player, @NonNull Location location) {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingMcMMO()) {
            return false;
        }
        if (!ExperienceConfig.getInstance().isFishingExploitingPrevented()) {
            return false;
        }
        McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null) {
            return false;
        }
        if (DEPRECATED_IS_EXPLOITING_FISHING != null) {
            return isDeprecatedOverfishing(mmoPlayer, location.toVector());
        }
        return mmoPlayer.getFishingManager().isExploitingFishing();
    }

    private static boolean isDeprecatedOverfishing(@NonNull McMMOPlayer mmoPlayer, @NonNull Vector vector) {
        // Should never pass, but this makes IntelliJ shut up about potential NPEs.
        if (DEPRECATED_IS_EXPLOITING_FISHING == null) {
            return false;
        }
        try {
            return (boolean) DEPRECATED_IS_EXPLOITING_FISHING.invoke(mmoPlayer.getFishingManager(), vector);
        } catch (InvocationTargetException | IllegalAccessException exception) {
            Logging.error("Failed to check for deprecated overfishing.", exception);
            return false;
        }
    }

    public static boolean canFishInWorld(@NonNull Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        List<String> whitelistedWorlds = MainConfig.getInstance().getAllowedWorlds();
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world.getName()) || whitelistedWorlds.contains(world.getKey().asString());
    }

    public static boolean canFishInRegion(@NonNull Location location) {
        return canUseRegion(location, MainConfig.getInstance().getAllowedRegions());
    }

    public static boolean canUseRegion(@NonNull Location location, @NonNull List<String> allowedRegions) {
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

    public static boolean isAFKFishing(@NonNull Player player) {
        if (!MainConfig.getInstance().isAFKProtectionEnabled()) {
            return false;
        }
        return AFKFishingTracker.get(player.getUniqueId()).isAFKFishing();
    }

}
