package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BiomeSetRequirementType extends RequirementType {

    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        World world = context.getWorld();
        Location location = context.getHookOrPlayerLocation();
        String configLocation = context.getConfigPath();
        if (configLocation == null) {
            configLocation = "N/A";
        }
        if (world == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        if (location == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not get location for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        Biome hookBiome = location.getBlock().getBiome();
        for (String value : values) {
            List<Biome> checkBiomes = MainConfig.getInstance().getBiomeSets().get(value);
            if (checkBiomes == null) {
                EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid biome set.");
                continue;
            }
            if (checkBiomes.contains(hookBiome)) {
                debugLogStatus(true, hookBiome.key().asString());
                return true;
            }
        }
        debugLogStatus(false, hookBiome.key().asString());
        return false;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "BIOME-SET";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
