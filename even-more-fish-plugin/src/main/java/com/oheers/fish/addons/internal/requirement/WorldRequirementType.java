package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WorldRequirementType extends RequirementType {

    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values) {
        World world = context.getWorld();
        if (world == null) {
            String configLocation = context.getConfigPath();
            if (configLocation == null) {
                configLocation = "N/A";
            }
            EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        String worldName = world.getName();
        for (String value : values) {
            if (worldName.equalsIgnoreCase(value)) {
                debugLogStatus(true, worldName);
                return true;
            }
        }
        debugLogStatus(false, worldName);
        return false;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "WORLD";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
